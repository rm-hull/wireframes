(ns wireframes.renderer.lighting
  (:require [inkspot.color :refer [coerce scale]]
            [wireframes.transform :refer [normal point]]))

(def default-position (point 10000 -10000 -1000000))

(defn- brightness [i c]
  (coerce (scale c i)))

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

(defn positional-lighting-decorator [lighting-position color-fn]
  (let [lighting-fn (compute-lighting lighting-position)]
    (fn [points-3d transformed-points polygon]
      (let [intensity (->>
                        polygon
                        (map transformed-points)
                        (apply normal)
                        (lighting-fn))]
        (brightness
          intensity
          (color-fn points-3d transformed-points polygon))))))