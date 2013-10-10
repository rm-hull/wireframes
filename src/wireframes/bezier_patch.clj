(ns wireframes.bezier-patch)

(defn- add
  "Add two points"
  [pa pb]
  (mapv + pa pb))

(defn- mult
  "Multiply a point [x,y,z] by a constant c"
  [pt c]
  (mapv (partial * c) pt))

(defn- evaluate-bezier-curve [t p]
  (let [omt (- 1 t) ; = one-minus-t
        b   [(* omt omt omt)
             (* 3 t omt omt)
             (* 3 t t omt)
             (* t t t)]]
    (reduce add (map mult p b))))

(defn- evaluate-bezier-patch [control-points u v]
  (let [u-curve (mapv
                  (partial evaluate-bezier-curve u)
                  (partition 4 control-points))]
    (evaluate-bezier-curve v u-curve)))

(defn surface-points [divisions vertices patch]
  (let [control-points (map vertices patch)
        divisions (double divisions)]
    (vec
      (for [j (range (inc divisions))
            i (range (inc divisions))]
        (evaluate-bezier-patch
          control-points
          (/ i divisions)
          (/ j divisions))))))
