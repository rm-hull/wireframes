(ns wireframes.renderer.canvas
  (:require [wireframes.transform :as t])
  (:use [wireframes.renderer :only [get-3d-points get-2d-points priority-fill shader compute-scale]]
        [wireframes.renderer.color :only [adjust-color create-color]]
        [monet.canvas :only [save restore stroke-width stroke-cap stroke-style fill fill-style
                             begin-path line-to move-to stroke close-path scale translate]]))

(defn walk-polygon [ctx points-2d polygon]
  (let [[ax ay] (get points-2d (first polygon))]
    (->
      ctx
      (begin-path)
      (move-to ax ay))
    (loop [ps (next polygon)]
      (if (nil? ps)
        (-> ctx close-path)
        (let [[bx by] (get points-2d (first ps))]
          (line-to ctx bx by)
          (recur (next ps)))))))

(defn wireframe-draw-fn [ctx points-2d fill-color edge-color]
  (->
    ctx
    (stroke-style edge-color)
    (fill-style fill-color))
  (fn [polygon]
    (->
      ctx
      (walk-polygon points-2d polygon)
      (fill)
      (stroke))))

(defn shader-draw-fn [ctx points-2d shader]
  (fn [polygon]
    (let [color (shader polygon)]
      (->
        ctx
        (walk-polygon points-2d polygon)
        (fill-style color)
        (stroke-style color)
        (fill)
        (stroke)))))

(defn draw-solid [{:keys [focal-length transform shape fill-color lighting-position style]} ctx]
  (let [priority-fill (priority-fill memoize)
        fill-color (adjust-color style fill-color)
        points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)
        polygons  (cond
                    (= style :transparent) (:polygons shape)
                    (= style :shaded)      (sort-by (priority-fill points-3d) (t/reduce-polygons (:polygons shape)))
                    :else                  (sort-by (priority-fill points-3d) (:polygons shape)))
        draw-fn   (if (= style :shaded)
                    (shader-draw-fn ctx points-2d (shader points-3d (create-color fill-color) lighting-position))
                    (wireframe-draw-fn ctx points-2d fill-color "rgb(0,0,0)"))]
  (doseq [polygon polygons]
    (draw-fn polygon)))
  ctx)

(defn ->canvas [ctx]
  (fn [draw-fn [w h]]
    (let [s (compute-scale w h)
          sw (double (/ 0.5 w))]
      (->
        ctx
        (save)
        (stroke-style :black)
        (translate (double (/ w 2)) (double (/ h 2)))
        (scale s s)
        (stroke-width sw)
        (draw-fn)
        (restore)))))
