(ns wireframes.shapes.platonic-solids
  (:require [wireframes.shapes.primitives :as p]
            [wireframes.transform :as t]))

(def epsilon 0.0000001)

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

(def dodecahedron nil)

(def icosahedron
  "A 20-sided polyhedron"
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