;; SVG renderer

^{:cljs
  (ns wireframes.renderer.vector
    (:require [wireframes.transform :as t])
    (:use [clojure.string :only [join]]
          [dommy.template :only [->node-like]]
          [wireframes.renderer :only [compute-scale priority-fill get-3d-points get-2d-points shader order-polygons]]
          [wireframes.renderer.color :only [adjust-color create-color rgba]])
    (:use-macros [dommy.macros :only [node]]))
}
(ns wireframes.renderer.vector
  (:require [wireframes.transform :as t])
  (:use [clojure.string :only [join]]
        [hiccup.core :only [html]]
        [wireframes.renderer :only [compute-scale priority-fill get-3d-points get-2d-points shader order-polygons]]
        [wireframes.renderer.color :only [adjust-color create-color rgba]]))

(defn- transform [w h]
  (let [scale (compute-scale w h)]
    (str
      "translate(" (double (/ w 2)) "," (double (/ h 2)) ") "
      "scale(" scale "," scale ")")))

(defn walk-polygon [points-2d polygon]
  (letfn [(directive [cmd p]
            (let [[x y] (get points-2d p)]
              (str cmd x "," y " ")))]
      (str
        (directive "M" (first polygon))
        (apply str (map (partial directive "L") (rest polygon)))
        "Z")))

(defn- style [fill-color edge-color sw]
  (str
    "stroke-width:" sw ";"
    "stroke:" (rgba edge-color) ";"
    "fill:" (rgba fill-color) ";"))

(defn wireframe-draw-fn [points-2d fill-color edge-color sw]
  (let [style (style fill-color edge-color sw)]
  (fn [polygon]
    [:path
     {:style style :d (walk-polygon points-2d polygon)}])))

(defn shader-draw-fn [points-2d shader sw]
  (fn [polygon]
    (let [color (shader polygon)]
      [:path {:style (style color color sw) :d (walk-polygon points-2d polygon)}])))

(defn draw-solid [{:keys [focal-length transform shape fill-color lighting-position style]}]
  (let [stroke-width (/ 0.5 800) ; TODO parameterize this
        fill-color (adjust-color style fill-color)
        points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)
        key-fn    ((priority-fill memoize) points-3d)
        draw-fn   (if (= style :shaded)
                    (shader-draw-fn points-2d (shader points-3d (create-color fill-color) lighting-position) stroke-width)
                    (wireframe-draw-fn points-2d fill-color "rgb(0,0,0)" stroke-width))]
  (for [polygon (order-polygons style key-fn shape)]
    (draw-fn polygon))))

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

