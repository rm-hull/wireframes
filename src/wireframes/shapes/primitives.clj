(ns wireframes.shapes.primitives
  (:require [clojure.core.rrb-vector :as fv]
            [wireframes.transform :as t]))

;; Shapes are represented as:
;;
;;    {:points [pt1 pt2 ...]
;;     :lines [l1 l2 ...]          l_n = 2 indices into points array
;;     :polygons [pg1 pg2 ...]}    pg_n = 3 indices into points array

(defn transform-shape [transform]
  (fn [{:keys [points] :as shape}]
    (assoc shape :points (mapv (partial t/transform-point transform) points))))

(defn- offsets [coll1 coll2]
  (mapv #(mapv (partial + (count coll1)) %) coll2))

(defn augment
  "Add two or more shapes together"
  ([shape1 shape2]
    (let [offsets  (partial offsets (:points shape1))
          adjusted (assoc shape2
                     :lines (offsets (:lines shape2))
                     :polygons (offsets (:polygons shape2)))]
      (merge-with (comp vec concat) shape1 adjusted)))
  ([shape1 shape2 & more]
    (let [initial (augment shape1 shape2)]
      (reduce augment initial more))))

(defn- connect-points [extruded-shape new-part]
  (let [e (count (:points extruded-shape))
        n (count (:points new-part))
        lines (->> (range n e) (map #(vector (- % n) %)))]
    (merge-with fv/catvec
      extruded-shape
      {:lines (fv/vec lines)})))

(defn- connect-polygons [extruded-shape new-part offset]
  (let [num-points (count (:points new-part))
        polygons   (for [a (range offset (+ offset num-points -1))
                         :let [b (inc a)
                               d (+ b num-points)
                               c (dec d)]]
                     [a b d c])]
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
             (connect-points new-part)
             (connect-polygons new-part next-index))
           (next generator)))))))

(defn make-point
  "Create a shape consisting of a single point"
  [x y z]
  {:points [[(double x) (double y) (double z)]]})

(defn make-line
  "Creates a joined line consisting of the points of the form [x y z]"
  [& points]
  (apply merge-with fv/catvec
    {:lines (->> points (map :points) count range (partition 2 1) (mapv vec))}
    points))

(defn make-grid [x y w h]
  (->
     (make-point x y 0)
     (extrude (t/translate 1 0 0) w)
     (extrude (t/translate 0 1 0) h)))

(defn degrees->radians [d]
  (/ (* (double d) Math/PI) 180.0))

(defn compute-bounds
  "Calculates the minimum and maximum bounds for the shape"
  [shape]
  (throw (Exception. "Not yet implemented")))
