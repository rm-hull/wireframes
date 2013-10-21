(ns wireframes.shapes.curved-solids
  (:require [wireframes.shapes.primitives :as p]
            [wireframes.bezier :as b]
            [wireframes.transform :as t]))

(def intervals->radians
  (let [atan8 (* (Math/atan 1.0) 8.0)]
    (fn  [num-intervals]
      (/ atan8 (double num-intervals)))))

(defn make-circle
  "Approximate a circle in the X-Y plane around the origin wth radius r and n points"
  [^double r n]
  (p/extrude
    (p/make-point (Math/max 0.0 r) 0 0)
    (t/rotate :z (intervals->radians n))
    n))

(defn make-bezier-spline [control-points n]
  {:points (->>
             (b/line-points n control-points)
             (mapv (partial apply t/point)))})

(defn make-torus
  "Approximate a torus with major radius r2 and minor radius r1,
   with correspondingly n2 and n1 points around each axis."
  [r1 r2 n1 n2]
  (let [move (p/transform-shape (t/translate r2 0 0))
        circle (move (make-circle r1 n1))]
    (p/extrude
      circle
      (t/rotate :y (intervals->radians n2))
      n2)))

(defn make-cylinder [r n h]
  (p/extrude
    (make-circle r n)
    (t/translate 0 0 h)
    1))

(defn make-cone [r n h]
  (p/extrude
    (make-circle r n)
    (t/combine
      (t/translate 0 0 1)
      (t/scale 0.9))
    h))

(defn make-star [r1 r2 n]
  (let [angle (intervals->radians (* n 2))]
    (p/extrude
      (map #(p/make-point (* %2 (Math/cos %1)) (* %2 (Math/sin %1)) 0)
           (iterate (partial + angle) 0)
           (cycle [r1 r2]))
      (* n 2))))

(defn make-sphere
  "Approximate a sphere at the origin wth radius r and n points"
  [r n]
  (let [angle (intervals->radians (* n 2))]
    (p/extrude
      (map #((p/transform-shape (t/translate 0 0 (* r (Math/cos %))))
               (make-circle (* r (Math/sin %)) n))
           (iterate (partial + angle) 0))
      n)))

(defn make-wineglass [n]
  (p/extrude
    (apply p/augment
      (for [control-points [[[ 0.000 0.425 0.000] [-0.007 0.412 0.000] [ 0.136 0.448 0.000] [ 0.151 0.446 0.000]]
                            [[ 0.151 0.446 0.000] [ 0.167 0.444 0.000] [ 0.161 0.447 0.000] [ 0.160 0.432 0.000]]
                            [[ 0.160 0.432 0.000] [ 0.159 0.417 0.000] [ 0.044 0.421 0.000] [ 0.023 0.401 0.000]]
                            [[ 0.023 0.401 0.000] [ 0.012 0.391 0.000] [ 0.009 0.381 0.000] [ 0.019 0.283 0.000]]
                            [[ 0.019 0.283 0.000] [ 0.022 0.252 0.000] [ 0.152 0.278 0.000] [ 0.180 0.067 0.000]]
                            [[ 0.180 0.067 0.000] [ 0.188 0.008 0.000] [ 0.176 0.045 0.000] [ 0.174 0.060 0.000]]
                            [[ 0.174 0.060 0.000] [ 0.167 0.106 0.000] [ 0.142 0.188 0.000] [ 0.109 0.217 0.000]]
                            [[ 0.109 0.217 0.000] [ 0.068 0.253 0.000] [ 0.034 0.258 0.000] [ 0.000 0.259 0.000]]]]
        (make-bezier-spline control-points 10)))
    (t/rotate :y (intervals->radians n))
    n))
