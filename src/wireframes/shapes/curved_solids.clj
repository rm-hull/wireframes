(ns wireframes.shapes.curved-solids
  (:require [wireframes.shape-primitives :as sp]
            [wireframes.transform :as t]))

(defn- see-saw
  "Seesaw between 0.0 and 1.0 with the given interval (which must be in the
   range 0..1). Note that using dobules will likely result in rounding errors."
  [interval]
  (let [steps (int (/ 1 interval))]
    (reductions + 0
      (cycle
        (concat
          (repeat steps interval)
          (repeat steps (- interval)))))))

(defn- curve
  "Defines the curve of unit radius at interval i, where i = 0..1"
  [i]
  (cond
    (< i 0.0) 0.0
    (> i 1.0) 1.0
    :else     (Math/sqrt (- (* 2 i) (* i i)))))

(defn- intervals->radians [num-intervals]
   (/ (* (Math/atan 1) 8.0) (double num-intervals)))

(defn make-circle
  "Approximate a circle in the X-Y plane around the origin wth radius r and n points"
  [^double r n]
  (sp/extrude
    (sp/make-point (Math/max 0.0 r) 0 0)
    (t/rotate :z (intervals->radians n))
    n))

(defn make-torus
  "Approximate a torus with major radius r2 and minor radius r1,
   with correspondingly n2 and n1 points around each axis."
  [r1 r2 n1 n2]
  (let [move (sp/transform-shape (t/translate r2 0 0))
        circle (move (make-circle r1 n1))]
    (sp/extrude
      circle
      (t/rotate :y (intervals->radians n2))
      n2)))

(defn make-cylinder [r n h]
  (sp/extrude
    (make-circle r n)
    (t/translate 0 0 h)
    1))

(defn make-cone [r n h]
  (sp/extrude
    (make-circle r n)
    (t/concat
      (t/translate 0 0 1)
      (t/scale 0.9))
    h))

(defn make-sphere
  "Approximate a sphere at the origin wth radius r and n points"
  [r n]
  (let [angle (intervals->radians (* n 2))]
    (sp/extrude
      (map #((sp/transform-shape (t/translate 0 0 (* r (Math/cos %))))
               (make-circle (* r (Math/sin %)) n))
           (iterate (partial + angle) 0))
      n)))
