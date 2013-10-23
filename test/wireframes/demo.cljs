(ns wireframes.demo
  (:use [wireframes.renderer.canvas :only [draw-solid ->canvas]]
        [wireframes.transform :only [combine rotate translate degrees->radians]]
        [wireframes.shapes.curved-solids :only [make-torus]]
        [wireframes.shapes.platonic-solids :only [icosahedron]]
        [monet.canvas :only [get-context fill-rect fill-style]]
        [monet.core :only [animation-frame]]
        [jayq.core :only [$]]))

(def canvas ($ :canvas#demo))
(def ctx (get-context (.get canvas 0) "2d"))
(set! *print-fn* #(.log js/console %))

(def torus (make-torus 1 3 30 30))

(defn render [x y z]
  (-> ctx
    (fill-style "rgba(255,255,255,0.75")
    (fill-rect { :x 0 :y 0 :w 800 :h 600}))
  ((->canvas ctx)
    (partial draw-solid
      {:style :shaded
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
