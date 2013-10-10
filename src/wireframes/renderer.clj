(ns wireframes.renderer
  (:require [wireframes.transform :as t]))

(defn priority-fill [points polygon]
  (->>
    polygon
    (map (comp peek points))
    (reduce +)
    -))

(defn get-3d-points [transform shape]
  (mapv
    (partial t/transform-point transform)
    (:points shape)))

(defn get-2d-points [focal-length points-3d]
  (mapv
    (t/perspective focal-length)
    points-3d))
