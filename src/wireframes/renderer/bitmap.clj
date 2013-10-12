(ns wireframes.renderer.bitmap
  (:use [wireframes.renderer :only [get-3d-points get-2d-points priority-fill calculate-illumination]])
  (:require [wireframes.transform :as t]
            [wireframes.shape-primitives :as sp]
            [wireframes.shape-loader :as sl]
            [wireframes.shapes.platonic-solids :as ps]
            [wireframes.shapes.curved-solids :as cs])
  (:import [java.awt.image BufferedImage]
           [java.awt.geom AffineTransform GeneralPath Ellipse2D$Double]
           [java.awt Color Graphics2D RenderingHints BasicStroke GraphicsEnvironment]
           [javax.imageio ImageIO]))

(def create-color
  (memoize
    (fn [^long brightness]
      (let [brightness (Math/max 5 (Math/min brightness 250))]
        (Color. brightness brightness brightness)))))

(defn- draw-dot [^Graphics2D g2d [^double x ^double y] size]
  (.fill g2d (Ellipse2D$Double. (- x (/ size 2)) (- y (/ size 2)) size size)))

(defn- add-line [^GeneralPath path [^double ax ^double ay] [^double bx ^double by]]
  (doto path
    (.moveTo ax ay)
    (.lineTo bx by))
  path)

(defn- create-polygon [^GeneralPath path [[^double ax ^double ay] & more]]
  (doto path
    (.reset)
    (.moveTo ax ay))
  (doseq [[^double bx ^double by] more]
    (.lineTo path bx by))
  (.closePath path)
  path)

(defn draw-wireframe [^Graphics2D g2d focal-length transform shape]
  (let [path (GeneralPath.)
        points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)]

    ;(.setColor g2d Color/RED)
    ;(doseq [[idx1 idx2] (:lines shape)]
    ;  (draw-dot g2d (points-2d idx1) 0.008))

    ;(.setColor g2d Color/GREEN)
    ;(doseq [[idx1 idx2 idx3] (:polygons shape)]
    ;  (add-line path (points-2d idx1) (points-2d idx2))
    ;  (add-line path (points-2d idx1) (points-2d idx3))
    ;  (add-line path (points-2d idx2) (points-2d idx3))
    ;  )
    ;(.draw g2d path)
    ;(.reset path)

    (.setColor g2d Color/BLACK)
    (doseq [line (:lines shape)]
      (apply add-line path (map points-2d line)))
    (.draw g2d path)))

(defn draw-solid [^Graphics2D g2d focal-length transform shape & [alpha]]
  (let [path (GeneralPath.)
        points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)
        painters-polygons (sort-by (partial priority-fill points-3d) (:polygons shape))]
    (doseq [polygon painters-polygons]
      (->>
        polygon
        (map points-3d)
        (calculate-illumination)
        (create-color)
        (.setColor g2d))
      (.fill g2d (create-polygon path (map points-2d polygon))))))

(defn create-image [w h]
  (if (GraphicsEnvironment/isHeadless)
    (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
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
      (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_NORMALIZE)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY))
    (draw-wireframe g2d focal-length transform shape)
    ;(draw-solid g2d focal-length transform shape)
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
        (t/rotate :x (sp/degrees->radians -120))
        (t/translate 0 -1 40))
      (sl/load-shape "resources/newell-teapot/teapot" 16)
      [1000 900])
    "doc/gallery/solid-teapot.png")

  (write-png
    (->img
      10
      (t/concat
        (t/rotate :x (sp/degrees->radians -30))
        (t/translate 0 0 25))
      (sl/load-shape "resources/newell-teapot/teacup" 16)
      [1000 900])
    "doc/gallery/solid-teacup.png")

  (write-png
    (->img
      3
      (t/concat
        (t/rotate :z (sp/degrees->radians 65))
        (t/rotate :y (sp/degrees->radians -30))
        (t/translate 0 0 16))
      (cs/make-torus 1 3 60 60)
      [400 400])
    "doc/gallery/wireframe-torus.png")

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


  (write-png
    (->img
      3
      (t/concat
        ;(t/rotate :z (sp/degrees->radians 65))
        (t/rotate :x (sp/degrees->radians 60))
        (t/rotate :y (sp/degrees->radians -15))
        (t/translate 0 0 16)
      )
      (cs/make-sphere 3 30)
      [900 900])
    "doc/gallery/wireframe-sphere.png")


  (write-png
    (->img
      8
      (t/concat
        (t/rotate :z (sp/degrees->radians 15))
        (t/rotate :x (sp/degrees->radians  -20))
        (t/translate 0 -0.2 5.5)
      )
      (cs/make-wineglass 60)
      [900 900])
    "doc/gallery/wireframe-wineglass.png")
)

