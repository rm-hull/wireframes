(ns wireframes.shape-loader
  (:require [clojure.string :as str]
            [wireframes.shape-primitives :as sp]
            [wireframes.bezier-patch :as bp]))

(defn- parse-int [s]
  (Integer/parseInt s))

(defn- parse-double [s]
  (Double/parseDouble s))

(defn- parse-csv [converter line]
  (->>
    (str/split line #",")
    (map converter)))

(defn- create-vertices [vertices-data]
  (mapv (comp vec (partial parse-csv parse-double)) vertices-data))

(defn create-patches [patch-data]
  ; teapot indexes start at 1... decrement for zero-offset indexing
  (let [f (partial parse-csv (comp dec parse-int))]
    (mapv f patch-data)))

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
            dir {
                 ;:north (- divisions)
                 :east 1
                 :south divisions
                 ;:west  -1
                 }
            :let [src  (+ i (* j divisions))
                  dest (calculate-destination-index src dir i j divisions)]
            :when dest]
        (if (< src dest) [src dest] [dest src])))))

(defn- create-surface [divisions vertices patch]
  {:points (bp/surface-points divisions vertices patch)
   :lines  (face-connectivity (inc divisions))
   :polygons nil })

(defn load-shape [file divisions]
  (let [raw-data    (vec (str/split-lines (slurp file)))
        num-patches (Integer/parseInt (raw-data 0))
        patches     (create-patches (subvec raw-data 1 (inc num-patches)))
        vertices    (create-vertices (subvec raw-data (+ num-patches 2)))]
      (->>
        ;(take 2 (drop 5 patches))
        patches
        (map (partial create-surface divisions vertices))
        (reduce sp/augment)
        ; TODO: create polygons
        )))

(comment

  (load-shape "resources/newell-teapot/teapot" 16)

)
