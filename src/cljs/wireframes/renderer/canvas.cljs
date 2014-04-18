;; HTML5 canvas renderer

(ns wireframes.renderer.canvas
  (:require
    [wireframes.transform :as t]
    [wireframes.renderer :refer [get-3d-points get-2d-points priority-fill shader compute-scale order-polygons]]
    [monet.canvas :refer [save restore stroke-width stroke-cap stroke-style fill fill-style
                          begin-path line-to move-to stroke close-path scale translate]]))

(defn walk-polygon [ctx points-2d polygon]
  (let [vertices (:vertices polygon)
        [ax ay] (get points-2d (first vertices))]
    (->
      ctx
      (begin-path)
      (move-to ax ay))
    (loop [vs (next vertices)]
      (if (nil? vs)
        (-> ctx close-path)
        (let [[bx by] (get points-2d (first vs))]
          (line-to ctx bx by)
          (recur (next vs)))))))

(defn create-polygon-renderer [ctx points-2d fragment-shader-fn]
  (fn [polygon]
    (let [[fill-color edge-color] (fragment-shader-fn polygon)]
      (->
        ctx
        (walk-polygon points-2d polygon)
        (fill-style fill-color)
        (stroke-style edge-color)
        (fill)
        (stroke)))))

(defn draw-solid [{:keys [focal-length transform shape color-fn style]} ctx]
  (let [points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)
        key-fn    ((priority-fill memoize) points-3d)
        render-fn (create-polygon-renderer
                    ctx
                    points-2d
                    (shader (:points shape) points-3d color-fn))]
  (doseq [polygon (order-polygons style key-fn shape)]
    (render-fn polygon)))
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
