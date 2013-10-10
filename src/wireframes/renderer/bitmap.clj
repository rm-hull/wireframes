(ns wireframes.renderer.bitmap
  (:require [wireframes.transform :as t]
            [wireframes.shape-primitives :as sp]
            [wireframes.shape-loader :as sl]
            [wireframes.shapes.platonic-solids :as ps])
  (:import [java.awt.image BufferedImage]
           [java.awt.geom AffineTransform GeneralPath Ellipse2D$Double]
           [java.awt Color Graphics2D RenderingHints BasicStroke GraphicsEnvironment]
           [javax.imageio ImageIO]))

(defn- draw-dot [^Graphics2D g2d [^Double x ^Double y] size]
  (.fill g2d (Ellipse2D$Double. (- x (/ size 2)) (- y (/ size 2)) size size)))

(defn- draw-line [^GeneralPath path [^Double ax ^Double ay] [^Double bx ^Double by]]
  (doto path
    (.moveTo ax ay)
    (.lineTo bx by)))

(defn- draw-shape [^Graphics2D g2d focal-length transform shape]
  (let [path (GeneralPath.)
        points (mapv
                 (comp
                   (t/perspective focal-length)
                   (partial t/transform-point transform))
                 (:points shape))]

;    (.setColor g2d Color/RED)
;    (doseq [[idx1 idx2] (:lines shape)]
;      (draw-dot g2d (points idx1) 0.008))

    (.setColor g2d Color/BLACK)
    (doseq [[idx1 idx2] (:lines shape)]
      (draw-line path (points idx1) (points idx2)))
    (.draw g2d path)))

(defn create-image [w h]
  (if (GraphicsEnvironment/isHeadless)
    (BufferedImage. w h BufferedImage/TYPE_INT_RGB)
    (.createCompatibleImage
       (.getDefaultConfiguration
         (.getDefaultScreenDevice
           (GraphicsEnvironment/getLocalGraphicsEnvironment)))
       w h)))

(defn ->img [focal-length transform shape [w h]]
  (let [img (create-image w h)
        g2d (.createGraphics img)
        scale (min (quot w 2) (quot h 2))]
    (doto g2d
      (.setBackground Color/WHITE)
      (.clearRect 0 0 w h)
      (.setColor Color/BLACK)
      (.translate (quot w 2) (quot h 2))
      (.scale scale scale)
      (.setStroke (BasicStroke. (/ 1.0 (quot w 2))))
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY))
    (draw-shape g2d focal-length transform shape)
    (.dispose g2d)
    img))

(defn write-png [image filename]
  (ImageIO/write image "png" (clojure.java.io/file filename)))

(comment

  (write-png
    (->img
      10
      (t/concat
        (t/rotate :z (sp/degrees->radians 35))
        (t/rotate :x (sp/degrees->radians -70))
        (t/translate 0 -1 40))
      (sl/load-shape "resources/newell-teapot/teapot" 16)
      [600 600])
    "doc/gallery/teapot.png")

  (write-png
    (->img
      3
      (t/concat
        (t/rotate :z (sp/degrees->radians 65))
        (t/rotate :y (sp/degrees->radians -30))
        (t/translate 0 0 16))
      (sp/make-torus 1 3 60 60)
      [400 400])
    "torus-65.png")

  (write-png
    (->img
      3
      (t/concat
        (t/rotate :z (sp/degrees->radians 65))
        (t/rotate :y (sp/degrees->radians -30))
        (t/translate 0 0 16))
      ps/tetrahedron
      [400 400])
    "tetrahedron.png")
)

