(ns wireframes.shapes.primitives
  (:require ^{:cljs [cljs.core.rrb-vector :as fv]}
            [clojure.core.rrb-vector :as fv]
            [wireframes.transform :as t]))

;; Shapes are represented as:
;;
;;    {:points [pt1 pt2 ...]
;;     :polygons [pg1 pg2 ...]}    pg_n = 3 indices into points array

(defn transform-shape [transform]
  (fn [{:keys [points] :as shape}]
    (assoc shape :points (mapv (t/transform-point transform) points))))

(defn- offsets [n coll]
  (mapv #(mapv (partial + n) %) coll))

(defn augment
  "Add two or more shapes together"
  ([shape1 shape2]
    (let [n (count (:points shape1))
          adj (assoc shape2 :polygons (offsets n (:polygons shape2)))]
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
                     [a b d c])]
    (merge-with fv/catvec
      extruded-shape
      {:polygons (vec polygons)})))

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
    {:polygons (->> points (map :points) count range (partition 2 1) (mapv vec))}
    points))

(defn make-polygon [& points]
  (apply merge-with fv/catvec
    {:polygons (->> points (map :points) count range vec vector)}
    points))

(defn make-grid [x y w h]
  (->
     (make-point x y 0)
     (extrude (t/translate 1 0 0) w)
     (extrude (t/translate 0 1 0) h)))

(defn compute-bounds
  "Calculates the minimum and maximum bounds for the shape"
  [shape]
  (->>
    (:points shape)
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
