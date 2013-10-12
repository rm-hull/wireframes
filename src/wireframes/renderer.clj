(ns wireframes.renderer
  (:require [wireframes.transform :as t]))

(defn priority-fill [points polygon]
  (->>
    polygon
    (mapv (comp peek points))
    ;(second)
    (reduce +) ; min/max/+
    -))

(defn get-3d-points [transform shape]
  (mapv
    (partial t/transform-point transform)
    (:points shape)))

(defn get-2d-points [focal-length points-3d]
  (mapv
    (t/perspective focal-length)
    points-3d))

(def √3 (Math/sqrt 3))

(defn calculate-illumination
  "Lighting from (1,-1,-1) direction, results in range 0 .. 255"
  [points]
  (int (* 255 (/ (reduce - (apply t/normal points)) √3))))
