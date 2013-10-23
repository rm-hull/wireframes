(ns wireframes.renderer.color
  (:require [clojure.string :as string])
  (:use [wireframes.common :only [parse-int parse-double]])
  ^:clj
  (:import [java.awt Color]))

(defprotocol IColor
  (red [c])
  (green [c])
  (blue [c])
  (alpha [c]))

(defn rgb [xs]
  (map (comp parse-int string/trim) xs))

(defn color-vec [[_ & xs]]
  (vec
    (case (count xs)
      1 (->> xs first seq (partition 2) (map (comp #(parse-int % 16) (partial apply str))) vec)
      3 (rgb xs)
      4 (concat (rgb (take 3 xs))
          [(-> (last xs) string/trim parse-double)]))))

(defn string->color [s]
  (condp re-matches s
    #"#(.*)" :>> color-vec
    #"rgb\((.*),(.*),(.*)\)" :>> color-vec
    #"rgba\((.*),(.*),(.*),(.*)\)" :>> color-vec))

(extend-type ^{:cljs  cljs.core.PersistentVector} clojure.lang.PersistentVector
  IColor
  (red   [[r _ _ _]] r)
  (green [[_ g _ _]] g)
  (blue  [[_ _ b _]] b)
  (alpha [[_ _ _ a]] (or a 1.0)))

(extend-type java.lang.String
  IColor
  (red   [s] (red (string->color s)))
  (green [s] (green (string->color s)))
  (blue  [s] (blue (string->color s)))
  (alpha [s] (alpha (string->color s))))

^:clj
(extend-type java.awt.Color
  IColor
  (red   [c] (.getRed c))
  (green [c] (.getGreen c))
  (blue  [c] (.getBlue c))
  (alpha [c] (/ (.getAlpha c) 255.0)))

#_({:cljs
(extend-type array
  IColor
  (red   [[r _ _ _]] r)
  (green [[_ g _ _]] g)
  (blue  [[_ _ b _]] b)
  (alpha [[_ _ _ a]] a))})

(defn rgba
  ([color] (rgba (red color) (green color) (blue color) (alpha color)))
  ([r g b a] (str "rgba(" r "," g "," b "," a ")")))

(defn to-color
  "Converts to a color. rgb values should be an integer in the 0-255 range,
   whilst alpha channel is a double in the range 0.0 - 1.0"
  [r g b a]
  ^{:cljs '(rgba r g b a)}
  (Color.
    (int r)
    (int g)
    (int b)
    (int (* a 255))))

(defn adjust-color [style & [color]]
  (let [color (or color "rgb(255,255,255)")
        alpha (style {:transparent 0.0 :translucent 0.6 :opaque 1.0 :shaded 1.0})]
    (when alpha
      (to-color
        (red color)
        (green color)
        (blue color)
        alpha))))

(defn create-color
  ([material-color] (create-color material-color "rgb(0,0,0)"))
  ([material-color shadow-color]
    (let [material-color (or material-color "rgb(192,192,192)")
        r (double (red   material-color))
        g (double (green material-color))
        b (double (blue  material-color))
        a (double (alpha material-color))]
      (fn [intensity]
        (if intensity
            (to-color
              (int (* r intensity))
              (int (* g intensity))
              (int (* b intensity))
              a)
          shadow-color)))))
