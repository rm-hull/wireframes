;; SVG renderer

^{:cljs
  (ns wireframes.renderer.svg
    (:require
      [wireframes.transform :as t]
      [clojure.string :as str]
      [dommy.template :refer [->node-like]]
      [wireframes.renderer :refer [compute-scale priority-fill get-3d-points get-2d-points shader order-polygons]]
      [inkspot.color :refer [rgba]])
    (:use-macros [dommy.macros :only [node]]))
}
(ns wireframes.renderer.svg
  (:require
    [wireframes.transform :as t]
    [clojure.string :as str]
    [hiccup.core :refer [html]]
    [wireframes.renderer :refer [compute-scale priority-fill get-3d-points get-2d-points shader order-polygons]]
    [inkspot.color :refer [rgba]]))

(defn- transform [w h]
  (let [scale (compute-scale w h)]
    (str
      "translate(" (double (/ w 2)) "," (double (/ h 2)) ") "
      "scale(" scale "," scale ")")))

(defn walk-polygon [points-2d polygon]
  (let [vertices (:vertices polygon)
        directive (fn [cmd p]
                    (let [[x y] (get points-2d p)]
                      (str cmd x "," y " ")))]
      (str
        (directive "M" (first vertices))
        (apply str (map (partial directive "L") (rest vertices)))
        "Z")))
; TODO more efficient to str/join?


(defn- style [fill-color edge-color sw]
  (str
    "stroke-width:" sw ";"
    "stroke:" (rgba edge-color) ";"
    "fill:" (rgba fill-color) ";"))

(defn create-polygon-renderer [stroke-width points-2d fragment-shader-fn]
  (fn [polygon]
    (let [[fill-color edge-color] (fragment-shader-fn polygon)]
      [:path
        {:style (style fill-color edge-color stroke-width)
         :d (walk-polygon points-2d polygon)}])))

(defn draw-solid [{:keys [focal-length transform shape color-fn style]}]
  (let [stroke-width (/ 0.5 800) ; TODO parameterize this
        points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)
        key-fn    ((priority-fill memoize) points-3d)
        render-fn (create-polygon-renderer
                    stroke-width
                    points-2d
                    (shader (:points shape) points-3d color-fn))]
  (for [polygon (order-polygons style key-fn shape)]
    (render-fn polygon))))

(defn ->svg [draw-fn [w h]]
  (^{:cljs node} html
    [:svg
     { :xmlns "http://www.w3.org/2000/svg"
       :xmlns:xlink "http://www.w3.org/1999/xlink"
       :width w :height h
       :zoomAndPan "magnify"
       :preserveAspectRatio "xMidYMid meet"
       :overflow "visible"
       :version "1.0" }
      [:g {:transform (transform w h)}
        (draw-fn)]]))

