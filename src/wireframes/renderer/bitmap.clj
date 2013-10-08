(ns wireframes.renderer.bitmap
  (:refer-clojure :exclude [identity concat])
  (:use [wireframes.transform]
        [wireframes.shape])
  (:import [java.awt.image BufferedImage]
           [java.awt.geom AffineTransform GeneralPath]
           [java.awt Color Graphics2D RenderingHints BasicStroke GraphicsEnvironment]
           [javax.imageio ImageIO]))

(defn- draw-shape [^Graphics2D g2d transform shape]
  (let [points (mapv (comp perspective (partial transform-point transform)) (:points shape))
        path (GeneralPath.)]
    (doseq [[idx1 idx2] (:lines shape)
            :let [[ax ay] (points idx1)
                  [bx by] (points idx2)]]
      (doto path
        (.moveTo ax ay)
        (.lineTo bx by)))
    (.draw g2d path)))

(defn create-image [w h]
  (if (GraphicsEnvironment/isHeadless)
    (BufferedImage. w h BufferedImage/TYPE_INT_RGB)
    (.createCompatibleImage
       (.getDefaultConfiguration
         (.getDefaultScreenDevice
           (GraphicsEnvironment/getLocalGraphicsEnvironment)))
       w h)))

(defn ->img [transform shape [w h]]
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
    (draw-shape g2d transform shape)
    (.dispose g2d)
    img))

(defn write-png [image filename]
  (ImageIO/write image "png" (clojure.java.io/file filename)))

(comment

  (def angle (degrees->radians 65))

  (write-png
    (->img
      (concat
        (rotate angle)
        (transpose-axes :y :z)
        (rotate (/ angle 1.618))
        (transpose-axes :y :z)
        (translate 0 0 6))
      (make-torus 1 3 60 60)
      [400 400])
    "tourus-65.png")
)

