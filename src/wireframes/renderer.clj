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

(defn priority-fill [^doubles points-3d]
  (fn [polygon]
      (reduce (fn [s c] (- s (aget ^doubles (points-3d c) 2))) 0.0 polygon)))

(defn get-3d-points [matrix shape]
  (mapv
    (partial t/transform-point matrix)
    (:points shape)))

(defn get-2d-points [focal-length points-3d]
  (mapv
    (t/perspective focal-length)
    points-3d))
