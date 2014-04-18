;; Graphics2D renderer

(ns wireframes.renderer.bitmap
  (:use [wireframes.renderer :only [get-3d-points get-2d-points priority-fill shader compute-scale order-polygons]])
  (:require [wireframes.transform :as t]
            [potemkin :refer [fast-memoize]])
  (:import [java.awt.image BufferedImage]
           [java.awt.geom AffineTransform GeneralPath Ellipse2D$Double]
           [java.awt Color Graphics2D RenderingHints BasicStroke GraphicsEnvironment]
           [javax.imageio ImageIO]))

(defn walk-polygon [^GeneralPath path points-2d polygon]
  (let [vertices (:vertices polygon)
        [^double ax ^double ay] (get points-2d (first vertices))]
    (doto path
      (.reset)
      (.moveTo ax ay))
    (loop [ps (next vertices)]
      (when-let [[^double bx ^double by] (get points-2d (first ps))]
        (.lineTo path bx by)
        (recur (next ps))))
    (.closePath path)))

(defn create-polygon-renderer [^Graphics2D g2d points-2d fragment-shader-fn]
  (let [path (GeneralPath.)]
    (fn [polygon]
      (let [[fill-color edge-color] (fragment-shader-fn polygon)]
        (walk-polygon path points-2d polygon)
        (doto g2d
          (.setColor fill-color)
          (.fill path)
          (.setColor edge-color)
          (.draw path))))))

(defn draw-solid [{:keys [focal-length transform shape color-fn style]} ^Graphics2D g2d]
  (let [points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)
        key-fn    ((priority-fill fast-memoize) points-3d)
        render-fn (create-polygon-renderer
                    g2d
                    points-2d
                    (shader (:points shape) points-3d color-fn))]
    (doseq [polygon (order-polygons style key-fn shape)]
      (render-fn polygon))))

(defn ^BufferedImage create-image [w h]
  (if (GraphicsEnvironment/isHeadless)
    (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
    (.createCompatibleImage
       (.getDefaultConfiguration
         (.getDefaultScreenDevice
           (GraphicsEnvironment/getLocalGraphicsEnvironment)))
       w h)))

(defn ^Graphics2D create-graphics [^BufferedImage img]
  (let [g2d (.createGraphics img)]
    (doto g2d
      (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_NORMALIZE)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY))
    g2d))

(defn ->img [draw-fn [w h]]
  (let [img (create-image w h)
        g2d (create-graphics img)
        s   (compute-scale w h)]
    (doto g2d
      (.setBackground Color/WHITE)
      (.clearRect 0 0 w h)
      (.setColor Color/BLACK)
      (.translate (double (/ w 2)) (double (/ h 2)))
      (.scale s s)
      (.setStroke (BasicStroke. (/ 0.5 w))))
    (draw-fn g2d)
    (.dispose g2d)
    img))

(defn write-png [^BufferedImage image filename]
  (ImageIO/write image "png" (clojure.java.io/file filename)))
