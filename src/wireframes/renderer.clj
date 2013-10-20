(ns wireframes.renderer
  (:require [wireframes.transform :as t]
            [potemkin :refer [fast-memoize]]
            [taoensso.timbre.profiling :as profiling :refer [p profile]]))

(defn compute-lighting [lighting-position]
  (let [lx (double (get lighting-position 0))
        ly (double (get lighting-position 1))
        lz (double (get lighting-position 2))
        v  (Math/sqrt
             (+ (* lx lx)
                (* ly ly)
                (* lz lz)))]
    (fn [normal]
      (let [nx (double (get normal 0))
            ny (double (get normal 1))
            nz (double (get normal 2))
            dp (+ (* nx lx)
                  (* ny ly)
                  (* nz lz))]
        (Math/abs ;when-not (neg? dp)
          (/ dp (*
                  v
                  (Math/sqrt
                    (+ (* nx nx)
                       (* ny ny)
                       (* nz nz))))))))))

(defn shader [points-3d color-fn lighting-position]
  (let [posn (or lighting-position (t/point 10000 -10000 -1000000))
        lighting-fn (compute-lighting posn)]
    (fn [polygon]
      (->>
        polygon
        (map points-3d)
        (apply t/normal)
        (lighting-fn)
        (color-fn)))))

(defn priority-fill [points-3d]
  (fast-memoize
    (fn [polygon]
      (loop [acc    0.0
             count  0
             points polygon]
        (if-let [[_ _ ^double z] (get points-3d (first points))]
          (recur
            (- acc z)
            (inc count)
            (rest points))
          (/ acc count))))))

(defn get-3d-points [transform shape]
  (mapv
    (t/transform-point transform)
    (:points shape)))

(defn get-2d-points [focal-length points-3d]
  (mapv
    (t/perspective focal-length)
    points-3d))
