(ns wireframes.shape-primitives
  (:require [wireframes.transform :as t]))

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
  "Add two shapes together"
  [shape1 shape2]
  (let [offsets  (partial offsets (:points shape1))
        adjusted (assoc shape2
                   :lines (offsets (:lines shape2))
                   :polygons (offsets (:polygons shape2)))]
    (merge-with (comp vec concat) shape1 adjusted)))

(defn- connect-points [extruded-shape new-part]
  (let [e (count (:points extruded-shape))
        n (count (:points new-part))
        new-lines (->> (range n e) (mapv #(vector (- % n) %)))]
    (merge-with (comp vec distinct concat)
      extruded-shape
      {:lines new-lines})))

(defn- connect-triangles [extruded-shape new-part offset1 offset2]
  extruded-shape
;  (let [line (partial nth (:lines extruded-shape))]
;    (apply concat
;      (for [i (range (count (:lines new-part)))
;            :let [[ol0 ol1] (line (+ i offset1))
;                  [nl0 nl1] (line (+ i offset2))]]
;          [[ol0 ol1 nl0] [nl1 nl1 ol1]])))
  )

(defn extrude
  "Given a shape, make a more complicated shape by copying it through the
   transform n times, and connecting the corresponding points. This is more
   powerful than the usual kind of extrusion, and can be used to create fairly
   interesting shapes --- a snail shell from a circle, for instance."
  ([shape transform n]
   (extrude (iterate (transform-shape transform) shape) n))
  ([generator n]
   (loop [i             0
          line-base     0
          extruded-part (first generator)
          generator     (next generator)]
     (if (or (>= i n) (empty? generator))
       extruded-part
       (let [new-part (first generator)
             new-line-base (count (:lines new-part))]
         (recur
           (inc i)
           new-line-base
           (->
             extruded-part
             (augment new-part)
             (connect-points new-part)
             (connect-triangles new-part line-base new-line-base))
           (next generator)))))))

(defn make-point
  "Create a shape consisting of a single point"
  [x y z]
  {:points [[(double x) (double y) (double z)]]})

(defn degrees->radians [d]
  (/ (* (double d) Math/PI) 180.0))

