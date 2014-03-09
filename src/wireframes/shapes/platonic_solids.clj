(ns wireframes.shapes.platonic-solids
  (:require [wireframes.shapes.primitives :as p]
            [wireframes.transform :as t]))

(def epsilon 0.00001)

(defn- =approx [^double x ^double y]
  (< (Math/abs (- x y)) epsilon))

(def sqrt-2 (Math/sqrt 2))
(def sqrt-5 (Math/sqrt 5))

(def rho (/ (inc sqrt-5) 2))
(def -rho (- rho))

(def tetrahedron
  {:points [(t/point  1  0 (/ -1 sqrt-2))
            (t/point -1  0 (/ -1 sqrt-2))
            (t/point  0  1 (/  1 sqrt-2))
            (t/point  0 -1 (/  1 sqrt-2))]
   :polygons [{:vertices [0 1 2]}
              {:vertices [0 2 3]}
              {:vertices [0 1 3]}
              {:vertices [1 2 3]}]})


(def cube
  "Start with a square polygon, and extrude to a line alone the Z-plane."
  (->
    (p/make-polygon
      (p/make-point 0 0 0)
      (p/make-point 0 1 0)
      (p/make-point 1 1 0)
      (p/make-point 1 0 0))
    (p/extrude
      (t/translate 0 0 1) 1)
    (p/center-at-origin)))

(def octahedron
  "A regular octahedron composed of eight equilateral triangles"
  (let [points (vec
                 (apply concat
                   (for [a [-1 1]]
                     [(t/point a 0 0)
                      (t/point 0 a 0)
                      (t/point 0 0 a)])))
        polygons (vec
                   (for [a (range (count points))
                         b (range a)
                         c (range b)
                         :when (and
                                 (=approx (t/distance (points a) (points b)) sqrt-2)
                                 (=approx (t/distance (points a) (points c)) sqrt-2)
                                 (=approx (t/distance (points b) (points c)) sqrt-2))]
                     {:vertices [a b c]}))]
  {:points points :polygons polygons }))

(def dodecahedron
  "A 12-sided polyhedron with regular pentagonal faces"
  {:points (vec
             (apply concat
               (for [x [-1 1]
                     y [-1 1]
                     z [-1 1]]
                 (t/point x y z))

               (for [a [(/ -1 rho) (/ 1 rho)]
                     b [(- rho) rho]]
                 (map #(apply t/point %) (take 3 (partition 3 1 (cycle [0 a b])))))))
   :polygons [{:vertices [14 8 0 10 2]}
              {:vertices [14 8 4 13 6]}
              {:vertices [15 9 0 8 4]}
              {:vertices [15 9 1 11 5]}
              {:vertices [16 10 0 9 1]}
              {:vertices [16 10 2 12 3]}
              {:vertices [17 11 1 16 3]}
              {:vertices [17 11 5 19 7]}
              {:vertices [18 12 2 14 6]}
              {:vertices [18 12 3 17 7]}
              {:vertices [19 13 4 15 5]}
              {:vertices [19 13 6 18 7]}]})

(def icosahedron
  "A 20-sided polyhedron with triangular faces"
  (let [points (vec
                 (apply concat
                   (for [x [-1 1]
                         y [rho -rho]]
                     [(t/point 0 x y)
                      (t/point x y 0)
                      (t/point y 0 x)])))
        polygons (vec
                   (for [a (range (count points))
                         b (range a)
                         c (range b)
                         :when (and
                                 (=approx (t/distance (points a) (points b)) 2)
                                 (=approx (t/distance (points a) (points c)) 2)
                                 (=approx (t/distance (points b) (points c)) 2))]
                     {:vertices [a b c]}))]
  {:points points :polygons polygons }))
