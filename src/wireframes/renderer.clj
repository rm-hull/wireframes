(ns wireframes.renderer
  (:require [wireframes.transform :as t]))

(def √3 (Math/sqrt 3))

(defn calculate-illumination
  "Lighting from (1,-1,-1) direction, results in range 0 .. 255"
  [points]
  (int (* 255 (/ (reduce - (apply t/normal points)) √3))))

(defn shader [points-3d color-fn]
  (fn [polygon]
    (->>
      polygon
      (map points-3d)
      (calculate-illumination)
      (color-fn))))

(defn priority-fill [points-3d]
  (fn [polygon]
    (->>
      polygon
      (mapv #(nth (points-3d %) 2))
      ;(second)
      (reduce +) ; min/max/+
      -)))

(defn get-3d-points [transform shape]
  (mapv
    (partial t/transform-point transform)
    (:points shape)))

(defn get-2d-points [focal-length points-3d]
  (mapv
    (t/perspective focal-length)
    points-3d))
