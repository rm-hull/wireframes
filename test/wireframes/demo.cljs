(ns wireframes.demo
  (:use [wireframes.renderer.canvas :only [draw-solid ->canvas]]
        [wireframes.transform :only [combine rotate translate degrees->radians]]
        [wireframes.shapes.curved-solids :only [make-torus]]
        [monet.canvas :only [get-context]]
        [jayq.core :only [$]]
        ))

(def canvas ($ :canvas#demo))
(def ctx (get-context (.get canvas 0) "2d"))

(def torus (make-torus 1 3 60 60))

((->canvas ctx)
  (partial draw-solid
    {:style :transparent
     :focal-length 3
     :shape torus
     :transform (combine
                  (rotate :z (degrees->radians 65))
                  (rotate :y (degrees->radians -30))
                  (translate 0 0 16))})
  [800 600])
