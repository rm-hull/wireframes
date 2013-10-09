(ns wireframes.transform
  (:refer-clojure :exclude [identity concat]))

(defn translate
  "A quaternion of sorts"
  [x y z]
  [[1.0  0.0  0.0  (double x)]
   [0.0  1.0  0.0  (double y)]
   [0.0  0.0  1.0  (double z)]])

(defn scale
  ([s] (scale s s s))
  ([sx sy sz]
   [[sx   0.0  0.0  0.0]
    [0.0  sy   0.0  0.0]
    [0.0  0.0  sz   0.0]]))

(def identity
  (translate 0.0  0.0  0.0))

(defn rotate
  "Rotate around the given axis by theta radians"
  [axis theta]
  (let [s (Math/sin theta)
        c (Math/cos theta)]
    (condp = axis
      :x [[ 1.0   0.0   0.0   0.0]
          [ 0.0     c  (- s)  0.0]
          [ 0.0     s     c   0.0]]

      :y [[   c   0.0     s   0.0]
          [ 0.0   1.0   0.0   0.0]
          [(- s)  0.0     c   0.0]]

      :z [[   c  (- s)  0.0   0.0]
          [   s     c   0.0   0.0]
          [ 0.0   0.0   1.0   0.0]])))

(defn dot-product [a b]
  (reduce + (map * a b)))

(defn concat
  ([a b]
    (let [a (conj a [0.0  0.0  0.0  1.0])
          b (conj b [0.0  0.0  0.0  1.0])
          transposed  (apply mapv vector a)
          row-mult    (fn [x] (mapv (partial dot-product x) transposed))]
      (subvec (mapv row-mult b) 0 3)))
  ([a b & more]
    (let [initial (concat a b)]
      (reduce concat initial more))))

(defn transform-point [a point]
  (->
    (partial dot-product (conj point 1.0))
    (mapv a)))

(defn perspective
  "Perspective transform a point into 2D"
  [[x y z]]
  [(/ x z) (/ y z)])

(defn normal
  "Calculate the normal of a triangle"
  [[ax ay az] [bx by bz] [cx cy cz]]
  (let [[v10 v11 v12] [(- ax bx) (- ay by) (- az bz)]
        [v20 v21 v22] [(- bx cx) (- by cy) (- bz cz)]
        [n0  n1  n2]  [(- (* v11 v22) (- v12 v21))
                       (- (* v12 v20) (- v10 v22))
                       (- (* v10 v21) (- v11 v20))]
        mag (Math/sqrt (+ (* n0 n0) (* n1 n1) (* n2 n2)))]
    [ (/ n0 mag) (/ n1 mag) (/ n2 mag)]))
