(ns wireframes.bezier-patch)

(defn evaluate-bezier-curve [t [p0 p1 p2 p3]]
  (println "t" t)
  (println "p" p0 p1 p2 p3)
  (let [omt (- 1 t) ; = one-minus-t
        b0  (* omt omt omt)
        b1  (* 3 t omt omt)
        b2  (* 3 t t omt)
        b3  (* t t t)]
    (+ (* p0 b0)
       (* p1 b1)
       (* p2 b2)
       (* p3 b3))))

(defn evaluate-bezier-patch [control-points u v]
  (let [u-curve (mapv
                  (partial evaluate-bezier-curve u)
                  (partition 4 control-points))]
    (evaluate-bezier-curve v u-curve)))

(defn compute-control-points [patch vertices]
  (map vertices patch))

