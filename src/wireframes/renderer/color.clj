(ns wireframes.renderer.color
  (:require [wireframes.renderer.lighting :refer [default-position positional-lighting-decorator]]
            [inkspot.color :refer [coerce red green blue alpha]]
            [inkspot.color-chart :refer [spectrum color-mapper]]))


(defn tee [f]
  (fn [x]
    (f x)
    x))

(defn dup [x]
  [x x])

(def black-edge
  (let [black (coerce :black)]
    (fn [x] [x black])))

(defn adjust-color
  "Adjusts the opacity of the given color, which can be one of the following
   keywords:

      :transparent (0% opacity),
      :translucent (60% opacity),
      :opaque (100% opacity)

  Alternatively it can be specified as a float in the range 0.0 to 1.0"
  [color opacity]
  (let [alpha (cond
                (keyword? opacity) (get {:transparent 0.0 :translucent 0.6 :opaque 1.0} opacity 1.0)
                (and (>= opacity 0.0) (<= 1.0)) opacity
                :else 1.0)]
    [(red color) (green color) (blue color) alpha]))

(defn flat-color [color & [opacity]]
  "Creates a fragment shader function which colors polygons"
  (let [adjusted-color (adjust-color color opacity)]
     (fn [points-3d transformed-points polygon]
        adjusted-color)))

(defn get-z [[_ _ z _]]
  z)

(defn spectral-z [low high]
  "Creates a fragment shader function which colors polygons using
   a spectrum of colors where the original 3D points Z co-ordinate
   is mapped to a specific color (blue low .. red high)."
  (let [colors (color-mapper (reverse (spectrum 100)) low high)]
    (fn [points-3d transformed-points polygon]
      (->>
       (:vertices polygon)
       (map (comp get-z points-3d))
       (reduce +)
       (* 0.33)
       colors))))

(defn wireframe
  "Creates a [fill edge] fragment shader function which colors polygons
   with a flat color fill with a black outline."
  [& [color opacity]]
  (comp
    black-edge
    (flat-color (or color :white) opacity)))

(defn solid
  "Creates a [fill edge] fragment shader function which colors polygons
   with a shaded color and no perceptible outline (i.e. the same color as
   the fill), with a light source at the given position."
  [& [color lighting-position]]
  (let [color  (coerce (or color :white))
        rgba [(red color) (green color) (blue color) (alpha color)]]
        ; more efficient to store this as a vector rather than coerce
        ; to native format
    (comp
      dup
      (positional-lighting-decorator
        (or lighting-position default-position)
        (flat-color rgba)))))
