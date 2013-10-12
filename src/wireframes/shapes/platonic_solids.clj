(ns wireframes.shapes.platonic-solids
  (:require [wireframes.shapes.primitives :as p]
            [wireframes.transform :as t]))

(def √2 (Math/sqrt 2))
(def √5 (Math/sqrt 5))

(def φ (/ (inc √5) 2))
(def -φ (- φ))

(def tetrahedron
  {:points [[ 1.0  0.0 (/ -1 √2)]
            [-1.0  0.0 (/ -1 √2)]
            [ 0.0  1.0 (/  1 √2)]
            [ 0.0 -1.0 (/  1 √2)]]
   :lines  [[0 1] [1 2] [2 0] [1 3] [2 3] [0 3]]
   :polgons nil})

(def cube
  "Start with a point, extrude to a line alone the Z-plane, then extrude that
   line in the Y-axis to make a square... extrude again along the X-axis to
   complete the square. "
  (->
    (p/make-point 0 0 0)
    (p/extrude (t/translate 0 0 1) 1)
    (p/extrude (t/translate 0 1 0) 1)
    (p/extrude (t/translate 1 0 0) 1)))

(def octahedron nil)

(def dodecahedron nil)

(def icosahedron
  "A 20-sided polyhedron"
  (let [points (vec
                 (apply concat
                   (for [one [-1 1] rho [φ -φ]]
                     [[0 one rho] [one rho 0] [rho 0 one]])))
        lines (vec
                (for [a (range (count points))
                      b (range (count points))
                      :when (and (not= a b) (= (t/distance (points a) (points b)) 2.0))]
                  [a b]))]
  {:points points
   :lines lines
   :polygons nil }))



