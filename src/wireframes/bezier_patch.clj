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

(defn compute-control-points [vertices patch]
  (map vertices patch))

(defn surface-points [control-points divisions]
  (let [divisions (double divisions)]
    (vec
      (for [j (range divisions)
            i (range divisions)]
        (evaluate-bezier-patch
          control-points
          (/ i divisions)
          (/ j divisions))))))

(defn- calculate-destination-index [source-index [dir offset] i j divisions]
  (cond
    (and (= dir :north) (= j 0)) nil
    (and (= dir :east)  (= i (dec divisions))) nil
    (and (= dir :south) (= j (dec divisions))) nil
    (and (= dir :west)  (= i 0)) nil
    :else (+ source-index offset)))

(defn face-connectivity [divisions]
  (vec
    (distinct
      (for [j (range divisions)
            i (range divisions)
            dir {:north (- divisions) :east 1 :south divisions :west  -1}
            :let [src  (+ i (* j divisions))
                  dest (calculate-destination-index src dir i j divisions)]
            :when dest]
        (if (< src dest) [src dest] [dest src])))))
