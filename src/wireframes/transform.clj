(ns wireframes.transform
  (:refer-clojure :exclude [identity]))

(defn degrees->radians [d]
  (/ (* (double d) Math/PI) 180.0))

(defn point
  ([[x y z]] (point x y z))
  ([x y z]   (mapv double [x y z 1])))

(defn matrix [& rows]
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

(defn dot-product [as bs]
  (reduce + (map * as bs)))

(defn transform-point [matrix point]
  (mapv
    (partial dot-product point)
    matrix))

(defn transpose [matrix]
  (apply mapv vector matrix))

(defn combine
  ([a b]
    (let [transposed (transpose a)]
      (mapv #(transform-point transposed %) b)))
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

(defn triangulate [points]
  (when (>= (count points) 3)
    (loop [acc []
           [a b c & more] points]
      (if (empty? more)
        (conj acc [a b c])
        (recur
          (conj acc [a b c])
          (cons a (cons c more)))))))
