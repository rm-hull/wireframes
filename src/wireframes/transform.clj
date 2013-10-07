(ns wireframes.transform
  (:refer-clojure :exclude [identity concat]))

(defn translate [x y z]
  [[1 0 0 x]
   [0 1 0 y]
   [0 0 1 z]])

(defn identity []
  (translate 0 0 0))

(defn rotate
  "Rotation aroiund the Z-axis"
  [theta]
  (let [s (Math/sin theta)
        c (Math/cos theta)]
    [[c (- s) 0 0]
     [s    c  0 0]
     [0    0  1 0]]))

(def transpose-axes
  "Exchange two of the X, Y, Z axes - useful for making rotate go around another axis"
  (let [axes {:x 0 :y 1 :z 2}]
    (fn [a b]
      (let [a-index (axes a)
            b-index (axes b)
            v       (identity)]
        (assoc v
          a-index (nth v b-index)
          b-index (nth v a-index))))))

(defn concat [a b]
  (let [a (conj a [0 0 0 1])
        b (conj b [0 0 0 1])
        transposed  (apply map vector b)
        dot-product (fn [x y] (reduce + (map * x y)))
        row-mult    (fn [x] (vec (map (partial dot-product x) transposed)))]
    (vec (take 3 (map row-mult a)))))


