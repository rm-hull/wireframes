(ns wireframes.demo
  (:require
    [wireframes.renderer.color :refer [wireframe solid flat-color black-edge]]
    [wireframes.renderer.lighting :refer [positional-lighting-decorator default-position]]
    [wireframes.renderer.canvas :refer [draw-solid ->canvas]]
    [wireframes.transform :refer [combine rotate translate degrees->radians]]
    [wireframes.shapes.curved-solids :refer [make-torus make-isosphere make-mobius-strip]]
    [wireframes.shapes.platonic-solids :refer [dodecahedron icosahedron]]
    [monet.canvas :refer [get-context fill-rect fill-style text stroke-style]]
    [monet.core :refer [animation-frame]]))

(def canvas (.getElementById js/document "demo"))
(def ctx (get-context canvas "2d"))
(enable-console-print!)

(def torus (make-torus 1 3 30 30))

(def isosphere (make-isosphere 3 2))

(def mobius-strip (make-mobius-strip 50 10))

(defn render [x y z]
  (->
    ctx
    (fill-style "rgba(255,255,255,0.75")
    (fill-rect { :x 0 :y 0 :w 800 :h 600}))

  ((->canvas ctx)
    (partial draw-solid
      {:style :translucent
       :color-fn (wireframe 0xeaf5fc :translucent)
       ;:style :shaded
       ;:color-fn (solid [0x0E 0xAF 0x5F])
       :focal-length 3
       :shape mobius-strip
       :transform (combine
                    (rotate :x (degrees->radians x))
                    (rotate :y (degrees->radians y))
                    (rotate :z (degrees->radians z))
                    (translate 0 0 8))})
    [800 600])

  (->
    ctx
    (fill-style :black)
    (text {:text (pr-str x y z) :x 30 :y 30})))

(defn animate []
  (letfn [(loop [x y z]
            (fn []
              (animation-frame
                (loop (+ x 0.3) (- y 0.7) (+ z 0.5))
                (render x y z))))]
    ((loop 0 65 -35))))

(animate)
