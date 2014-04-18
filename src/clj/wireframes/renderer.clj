(ns wireframes.renderer
  (:require [wireframes.transform :as t]))

(defn shader [points-3d transformed-points color-fn]
  (fn [polygon]
    (color-fn points-3d transformed-points polygon)))

(defn priority-fill [cache-fn]
  (fn [points-3d]
    (cache-fn
      (fn [polygon]
        (loop [acc    0.0
               count  0
               points (:vertices polygon)]
          (if (empty? points)
            (/ acc count)
            (let [[_ _ ^double z] (get points-3d (first points))]
              (recur
                (- acc z)
                (inc count)
                (rest points)))))))))

(defn get-3d-points [transform shape]
  (mapv
    (t/transform-point transform)
    (:points shape)))

(defn get-2d-points [focal-length points-3d]
  (mapv
    (t/perspective focal-length)
    points-3d))

(defn compute-scale [w h]
  (double (min (/ w 2) (/ h 2))))

(defn order-polygons [style keyfn shape]
  (let [polygons (:polygons shape)]
    (condp = style
      :transparent polygons
      :shaded      (sort-by keyfn (t/reduce-polygons polygons))
                   (sort-by keyfn polygons))))