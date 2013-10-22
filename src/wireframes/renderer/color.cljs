(ns wireframes.renderer.color
  (:require [clojure.string :as string])
  (:use [wireframes.common :only [parse-int parse-double]])
  )

(defprotocol IColor
  (red [c])
  (green [c])
  (blue [c])
  (alpha [c]))

(extend-type cljs.core.PersistentVector
  IColor
  (red   [[r _ _ _]] r)
  (green [[_ g _ _]] g)
  (blue  [[_ _ b _]] b)
  (alpha [[_ _ _ a]] a))

(defn rgb [xs]
  (map (comp parse-int string/trim) xs))

(defn color-vec [[_ & xs]]
  (vec
    (case (count xs)
      1 (->> (partition 2 xs) (mapv (comp #(parse-int % 16) (partial apply str))))
      3 (rgb xs)
      4 (concat (rgb (take 3 xs))
          [(-> (last xs) string/trim parse-double)]))))

(defn string->color [s]
  (condp re-matches s
    #"#(.*)" :>> color-vec
    #"rgb\((.*),(.*),(.*)\)" :>> color-vec
    #"rgba\((.*),(.*),(.*),(.*)\)" :>> color-vec))

(extend-type string
  IColor
  (red   [s] (red (string->color s)))
  (green [s] (green (string->color s)))
  (blue  [s] (blue (string->color s)))
  (alpha [s] (alpha (string->color s))))

(extend-type array
  IColor
  (red   [[r _ _ _]] r)
  (green [[_ g _ _]] g)
  (blue  [[_ _ b _]] b)
  (alpha [[_ _ _ a]] a))

(defn rgba [r g b a]

  (str "rgba(" r "," g "," b "," a ")"))

(defn adjust-color [style & [color]]
  (let [color (or color "rgb(255,255,255)")
        alpha (style {:transparent 0.0 :translucent 0.5 :opaque 1.0 :shaded 1.0})]
    (when alpha
      (rgba
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
            (rgba
              (int (* r intensity))
              (int (* g intensity))
              (int (* b intensity))
              a)
          shadow-color)))))
