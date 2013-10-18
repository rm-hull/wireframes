(ns wireframes.transform
  (:refer-clojure :exclude [identity vec])
  (:require [wireframes.common :as c]
            [clojure.core.rrb-vector :as fv]))

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
  (fn [[^double ax ^double ay ^double az ^double aw]]
    (mapv
      (fn [[^double bx ^double by ^double bz ^double bw]] (+ (* ax bx) (* ay by) (* az bz) (* aw bw)))
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
  (let [v10 (- ax bx)
        v11 (- ay by)
        v12 (- az bz)
        v20 (- bx cx)
        v21 (- by cy)
        v22 (- bz cz)
        n0  (- (* v11 v22) (* v12 v21))
        n1  (- (* v12 v20) (* v10 v22))
        n2  (- (* v10 v21) (* v11 v20))
        mag (Math/sqrt (+ (* n0 n0) (* n1 n1) (* n2 n2)))]
    [ (/ n0 mag) (/ n1 mag) (/ n2 mag)]))

(defn sqr [x]
  (* x x))

(defn distance
  "Distance between two points"
  [a b]
  (Math/sqrt (reduce + (map (comp sqr -) a b))))

(defn triangulate
  "Attempts to break down the polygon (defined by the points) into an array
   of triangles which represent the same surface area. NOTE: If the polygon
   is already triangular (or less) in nature, then that polygon is returned
   wrapped in an array."
  [points]
  (if (<= (count points) 3)
    [points]
    (loop [acc []
           [a b c & more] points]
      (if (empty? more)
        (conj acc [a b c])
        (recur
          (conj acc [a b c])
          (cons a (cons c more)))))))

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
