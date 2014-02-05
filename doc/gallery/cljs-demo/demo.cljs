(ns wireframes.demo
  (:require
    [wireframes.renderer.color :refer [wireframe solid flat-color black-edge]]
    [wireframes.renderer.lighting :refer [positional-lighting-decorator default-position]]
    [wireframes.renderer.canvas :refer [draw-solid ->canvas]]
    [wireframes.transform :refer [combine rotate translate degrees->radians]]
    [wireframes.shapes.curved-solids :refer [make-torus]]
    [wireframes.shapes.platonic-solids :refer [icosahedron]]
    [monet.canvas :refer [get-context fill-rect fill-style]]
    [monet.core :refer [animation-frame]]
    [jayq.core :refer [$]]))

(def canvas ($ :canvas#demo))
(def ctx (get-context (.get canvas 0) "2d"))
(enable-console-print!)

(def torus (make-torus 1 3 30 30))

(defn render [x y z]
  (-> ctx
    (fill-style "rgba(255,255,255,0.75")
    (fill-rect { :x 0 :y 0 :w 800 :h 600}))
  ((->canvas ctx)
    (partial draw-solid
      {:style :translucent
       :color-fn (wireframe 0xeaf5fc :translucent)
       ;:style :shaded
       ;:color-fn (solid :white)
       :focal-length 3
       :shape icosahedron
       :transform (combine
                    (rotate :x (degrees->radians x))
                    (rotate :y (degrees->radians y))
                    (rotate :z (degrees->radians z))
                    (translate 0 0 16))})
    [800 600]))

(defn animate []
  (letfn [(loop [x y z]
            (fn []
              (animation-frame
                (loop (+ x 0.3) (- y 0.7) (+ z 0.5))
                (render x y z))))]
    ((loop 0 65 -35))))

(animate)
