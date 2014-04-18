(ns wireframes.shapes.primitives
  (:require [clojure.core.rrb-vector :as fv]
            [wireframes.transform :as t]))

;; Shapes are represented as:
;;
;;    {:points [pt1 pt2 ...]
;;     :polygons [{:vertices pg1}
;;                {:vertices pg2} ...]}    pg_n = 3 indices into points array

(defn transform-shape [transform]
  (fn [{:keys [points] :as shape}]
    (assoc shape :points (mapv (t/transform-point transform) points))))

(defn- update-vertex-indices
  "Shifts the vertex indexes by the given offset, preserving any existing
   key/value pairs in the shape's polygons"
  [shape offset]
  (let [vec-updater (fn [xs] (mapv (partial + offset) xs))
        map-updater (fn [m] (update-in m [:vertices] vec-updater))]
    (update-in shape [:polygons] (partial mapv map-updater))))

(defn augment
  "Add two or more shapes together"
  ([shape1 shape2]
    (let [n (count (:points shape1))
          adj (update-vertex-indices shape2 n)]
    (merge-with fv/catvec shape1 adj)))
  ([shape1 shape2 & more]
    (let [initial (augment shape1 shape2)]
      (reduce augment initial more))))

(defn- connect-polygons [extruded-shape new-part offset]
  (let [num-points (count (:points new-part))
        polygons   (for [a (range offset (+ offset num-points -1))
                         :let [b (inc a)
                               d (+ b num-points)
                               c (dec d)]]
                     {:vertices [a b d c]})]
    (merge-with fv/catvec
      extruded-shape
      {:polygons (fv/vec polygons)})))

(defn extrude
  "Given a shape, make a more complicated shape by copying it through the
   transform n times, and connecting the corresponding points. This is more
   powerful than the usual kind of extrusion, and can be used to create fairly
   interesting shapes --- a snail shell from a circle, for instance."
  ([shape transform n]
   (extrude (iterate (transform-shape transform) shape) n))
  ([generator n]
   (loop [i             0
          next-index    0
          extruded-part (first generator)
          generator     (next generator)]
     (if (or (>= i n) (empty? generator))
       extruded-part
       (let [new-part (first generator)
             num-points (count (:points new-part))]
         (recur
           (inc i)
           (+ next-index num-points)
           (->
             extruded-part
             (augment new-part)
             (connect-polygons new-part next-index))
           (next generator)))))))

(defn make-point
  "Create a shape consisting of a single point"
  [x y z]
  {:points [(t/point x y z)]})

(defn make-line
  "Creates a joined line consisting of the points of the form [x y z]"
  [& points]
  (apply merge-with fv/catvec
    {:polygons (->> points (map :points) count range (partition 2 1) (mapv #(hash-map :vertices (vec %))))}
    points))

(defn make-polygon [& points]
  (apply merge-with fv/catvec
    {:polygons (->> points (map :points) count range vec (hash-map :vertices) vector)}
    points))

(defn make-grid [x y w h]
  (->
     (make-point x y 0)
     (extrude (t/translate 1 0 0) w)
     (extrude (t/translate 0 1 0) h)))

(defn mesh [x-divisions y-divisions]
  (for [j (range y-divisions)
        i (range x-divisions)
        :let [a (+ i (* j (inc x-divisions)))
              b (inc a)
              c (+ b x-divisions)
              d (inc c)]]
    {:vertices [a b d c]})) ; order of points is important

(defn make-surface [x-range y-range z-fn]
  {:points (vec
             (for [x x-range
                   y y-range]
               (t/point x y (z-fn x y))))
   :polygons (vec
               (mesh
                 (dec (count x-range))
                 (dec (count y-range))))})

(defn active-points
  "Filters points that only participate in polygon faces"
  [shape]
  (->>
    shape
    :polygons
    (apply merge-with concat)
    :vertices
    set
    (mapv #(get (:points shape) %))))

(defn compute-bounds
  "Calculates the minimum and maximum bounds for the shape"
  [shape]
  (->>
    (active-points shape) ;(:points shape)
    (apply map (juxt min max))
    (apply map (comp t/point vector))))

(defn center-at-origin
  "Determines the bounds of the shape, then shifts it to be centered at the origin.
   Note only the bounds are used to determine the central point, rather than the
   averaged centroids"
  [shape]
  (let [[[min-x min-y min-z] [max-x max-y max-z]] (compute-bounds shape)
        width  (- max-x min-x)
        height (- max-y min-y)
        depth  (- max-z min-z)
        transform (transform-shape
                    (t/translate
                      (- (- min-x) (/ width 2))
                      (- (- min-y) (/ height 2))
                      (- (- min-z) (/ depth 2))))]
    (transform shape)))
