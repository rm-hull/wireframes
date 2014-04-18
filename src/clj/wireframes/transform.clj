(ns wireframes.transform
  (:refer-clojure :exclude [identity vec])
  (:require [wireframes.common :as c]))

(defn degrees->radians [d]
  (/ (* (double d) Math/PI) 180.0))

(defn point
  "Constuctor for making points"
  ([[x y z]] (point x y z))
  ([x y z]   (mapv double [x y z 1])))

(defn vec
  "Convert a point back into a clojure vector"
  [point]
  (subvec point 0 3))

(defn matrix
  "Matrix builder"
  [& rows]
  (mapv #(mapv double %) rows))

(defn translate
  "A quaternion of sorts"
  [x y z]
  (matrix
    [1  0  0  x]
    [0  1  0  y]
    [0  0  1  z]
    [0  0  0  1]))

(defn scale
  ([s] (scale s s s))
  ([sx sy sz]
   (matrix
     [sx  0   0  0]
     [0  sy   0  0]
     [0   0  sz  0]
     [0   0   0  1])))

(defn rotate
  "Rotate around the given axis by theta radians"
  [axis theta]
  (let [s (Math/sin theta)
        c (Math/cos theta)]
    (condp = axis
      :x (matrix
           [   1     0     0   0]
           [   0     c  (- s)  0]
           [   0     s     c   0]
           [   0     0     0   1])

      :y (matrix
           [   c     0     s   0]
           [   0     1     0   0]
           [(- s)    0     c   0]
           [   0     0     0   1])

      :z (matrix
           [   c  (- s)    0   0]
           [   s     c     0   0]
           [   0     0     1   0]
           [   0     0     0   1]))))

(def identity
  (translate 0 0 0))

(defn dot-product [^doubles as ^doubles bs]
  (reduce + (mapv * as bs)))

(defn transform-point [matrix]
  (fn [[ax ay az aw]]
    (mapv
      (fn [[bx by bz bw]]
        (+
          (* (double ax) (double bx))
          (* (double ay) (double by))
          (* (double az) (double bz))
          (* (double aw) (double bw))))
      matrix)))

(defn transpose [matrix]
  (apply mapv vector matrix))

(defn combine
  ([a b]
    (let [transposed (transpose a)]
      (mapv (transform-point transposed) b)))
  ([a b & more]
    (let [initial (combine a b)]
      (reduce combine initial more))))

(defn perspective
  "Constructs a perspective function for a given focal-length, which
   can be used to project a 3D point into 2D cartesian co-ordinates."
  [focal-length]
  (let [focal-length (double focal-length)]
    (fn [[x y z]]
      (let [p (/ focal-length (- focal-length z))]
        [(* p x) (* p y)]))))

(defn normal
  "Calculate the normal of a triangle"
  [[ax ay az] [bx by bz] [cx cy cz]]
  (let [ux (double (- bx ax)) ; Vector U
        uy (double (- by ay))
        uz (double (- bz az))
        vx (double (- cx ax)) ; Vector V
        vy (double (- cy ay))
        vz (double (- cz az))]
    [(- (* uy vz) (* uz vy))
     (- (* uz vx) (* ux vz))
     (- (* ux vy) (* uy vx))]))

(defn centroid
  "Calculate the centroid of a triangle"
  [[ax ay az] [bx by bz] [cx cy cz]]
  [(double (/ (+ ax bx cx) 3))
   (double (/ (+ ay by cy) 3))
   (double (/ (+ az bz cz) 3))])


(defn mid-point
  "Calculate the midpoint of a line"
  [[ax ay az] [bx by bz]]
  [(double (/ (+ ax bx) 2))
   (double (/ (+ ay by) 2))
   (double (/ (+ az bz) 2))])

(defn sqr [x]
  (* x x))

(defn distance
  "Distance between two points"
  [a b]
  (Math/sqrt (reduce + (map (comp sqr -) a b))))

;; TODO - doesnt belong in this namespace
(defn triangulate
  "Attempts to break down the polygon (defined by the points) into an array
   of triangles which represent the same surface area. NOTE: If the polygon
   is already triangular (or less) in nature, then that polygon is returned
   wrapped in an array."
  [polygon]
  (if (<= (count (:vertices polygon)) 3)
    [polygon]
    (loop [acc []
           [a b c & more] (:vertices polygon)]
      (if (empty? more)
        (conj acc (assoc polygon :vertices [a b c]))
        (recur
          (conj acc (assoc polygon :vertices [a b c]))
          (cons a (cons c more)))))))

;; TODO - doesnt belong in this namespace
(defn reduce-polygons
  "Alter a sequence of polygons such that the output contains polygons
   with no more than 3 sides: hence polygons with 4 or more sides are
   split into triangles."
  [polygons]
  (loop [acc      (transient [])
         polygons polygons]
    (if (empty? polygons)
      (persistent! acc)
      (let [[p & ps] (triangulate (first polygons))]
        (recur
          (conj! acc p)
          (c/simple-concat ps (next polygons)))))))

(defn multiply [[x y z] n]
  (point (* x n) (* y n) (* z n)))

(defn divide [[x y z] n]
  (point (/ x n) (/ y n) (/ z n)))

(defn mag-sq [xyz]
  (dot-product xyz xyz))

(defn magnitude [[x y z]]
  (Math/sqrt (mag-sq [x y z])))

(defn normalize
  ([xyz] (normalize xyz 1.0))
  ([xyz scale]
    (let [m (double (/ (magnitude xyz) scale))]
      (if (and (not= m 0) (not= m 1))
        (divide xyz m)
        xyz))))