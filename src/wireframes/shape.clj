(ns wireframes.shape
  (:refer-clojure :exclude [identity concat])
  (:use [wireframes.transform]))

;; Shapes are represented as:
;;
;;    {:points [pt1 pt2 ...]
;;     :lines [l1 l2 ...]          l_n = 2 indices into points array
;;     :polygons [pg1 pg2 ...]}    pg_n = 3 indices into points array

(defn transform-shape [transform {:keys [points] :as shape}]
  (assoc shape :points (mapv (partial transform-point transform) points)))

(defn- offsets [coll1 coll2]
  (mapv #(mapv (partial + (count coll1)) %) coll2))

(defn augment
  "Add two shapes together"
  [shape1 shape2]
  (let [offsets (partial offsets (:points shape1))]
    (merge-with (comp vec clojure.core/concat)
      shape1
      {:points   (:points shape2)
       :lines    (offsets (:lines shape2))
       :polygons (offsets (:polygons shape2))})))

(defn- connect-points [extruded-shape new-part]
  (let [e (count (:points extruded-shape))
        n (count (:points new-part))
        new-lines (->> (range n e) (mapv #(vector (- % n) %)))]
    (merge-with (comp vec distinct clojure.core/concat)
      extruded-shape
      {:lines new-lines})))

(defn- connect-triangles [extruded-shape new-part offset1 offset2]
  extruded-shape
;  (let [line (partial nth (:lines extruded-shape))]
;    (apply clojure.core/concat
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
    (rotate (intervals->radians n))
    (make-point r 0 0)
    n))

(defn make-torus
  "Approximate a torus with major radius r2 and minor radius r1,
   with correspondingly n2 and n1 points around each axis."
  [r1 r2 n1 n2]
  (let [circle (transform-shape
                 (translate r2 0 0)
                 (make-circle r1 n1))]
    (extrude
      (concat
        (transpose-axes :y :z)
        (rotate (intervals->radians n2))
        (transpose-axes :y :z))
      circle
      n2)))

(defn make-cube
  "Start with a point, extrude to a line alone the Z-plane, then extrude that
   line in the Y-axis to make a square... extrude again along the X-axis to
   complete the square. "[n]
  (->>
    (make-point 0 0 0)
    (extrude (translate 0 0 n))
    (extrude (translate 0 n 0))
    (extrude (translate n 0 0))))

(defn make-cylinder [r n h]
  (->>
    (make-circle r n)
    (extrude (translate 0 0 h))))

(comment

  (def n 12)
  (def r 10)

  (clojure.pprint/pprint (make-circle 10 12))

  (clojure.pprint/pprint (make-cube 5))

  (clojure.pprint/pprint (make-torus 1 3 12 12))

  (def xf (rotate (/ (* (Math/atan 1) 8.0) (double n))))

  (clojure.pprint/pprint (take 10 (iterate (partial transform-shape xf) (make-point r 0 0))))

  (augment
    (make-point 1 1 1)
    (make-point 1 2 1))

  (offsets [[1 2]] )

  (def x
    (->
      (make-point 1 1 1)
      (augment (make-point 1 2 1))
      (connect-points (make-point 1 2 1))
        ))

  (def y
    (-> x
      (augment (make-point 1 3 1))
      (connect-points (make-point 1 3 1))))

  (def z
    (-> y
      (augment (make-point 1 1 5))
      (connect-points (make-point 1 1 5))))

)
