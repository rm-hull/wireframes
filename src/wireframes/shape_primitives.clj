(ns wireframes.shape-primitives
  (:require [wireframes.transform :as t]))

;; Shapes are represented as:
;;
;;    {:points [pt1 pt2 ...]
;;     :lines [l1 l2 ...]          l_n = 2 indices into points array
;;     :polygons [pg1 pg2 ...]}    pg_n = 3 indices into points array

(defn transform-shape [transform {:keys [points] :as shape}]
  (assoc shape :points (mapv (partial t/transform-point transform) points)))

(defn- offsets [coll1 coll2]
  (mapv #(mapv (partial + (count coll1)) %) coll2))

(defn augment
  "Add two shapes together"
  [shape1 shape2]
  (let [offsets (partial offsets (:points shape1))]
    (merge-with (comp vec concat)
      shape1
      {:points   (:points shape2)
       :lines    (offsets (:lines shape2))
       :polygons (offsets (:polygons shape2))})))

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
  ([transform shape]
   (extrude transform shape 1))
  ([transform shape n]
   (loop [i              0
          line-base      0
          shapes         (next (iterate (partial transform-shape transform) shape))
          extruded-part  shape]
     (if (>= i n)
       extruded-part
       (let [new-part (first shapes)
             new-line-base (count (:lines new-part))]
         (recur
           (inc i)
           new-line-base
           (next shapes)
           (->
             extruded-part
             (augment new-part)
             (connect-points new-part)
             (connect-triangles new-part line-base new-line-base))))))))

(defn make-point
  "Create a shape consisting of a single point"
  [x y z]
  {:points [[(double x) (double y) (double z)]]})

(defn- intervals->radians [num-intervals]
   (/ (* (Math/atan 1) 8.0) (double num-intervals)))

(defn degrees->radians [d]
  (/ (* (double d) Math/PI) 180.0))

(defn make-circle
  "Approximate a circle in the X-Y plane around the origin wth radius r and n points"
  [r n]
  (extrude
    (t/rotate :z (intervals->radians n))
    (make-point r 0 0)
    n))

(defn make-torus
  "Approximate a torus with major radius r2 and minor radius r1,
   with correspondingly n2 and n1 points around each axis."
  [r1 r2 n1 n2]
  (let [circle (transform-shape
                 (t/translate r2 0 0)
                 (make-circle r1 n1))]
    (extrude
      (t/rotate :y (intervals->radians n2))
      circle
      n2)))


(defn make-cylinder [r n h]
  (->>
    (make-circle r n)
    (extrude (t/translate 0 0 h))))

(defn make-cone [r n h]
  (extrude
    (concat
      (t/translate 0 0 1)
      (t/scale 0.9))
    (make-circle r n)
    h))


