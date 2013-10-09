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

(defn- create-points [vertices-data]
  (mapv (comp vec (partial parse-csv parse-double)) vertices-data))

(defn create-patches [patch-data]
  (let [f (partial parse-csv (comp dec parse-int))] ; teapot indexes start at 1...
    (->>                                            ; decrement for zero-offset indexing
      patch-data
      (map f))))

(defn- create-surface [divisions vertices patch]
  (let [control-points (bp/compute-control-points vertices patch)]
    {:points (bp/surface-points control-points divisions)
     :lines  (bp/face-connectivity divisions)
     :polygons nil }))

(defn load-shape [file divisions]
  (let [raw-data    (vec (str/split-lines (slurp file)))
        num-patches (Integer/parseInt (raw-data 0))
        patches     (create-patches (subvec raw-data 1 (inc num-patches)))
        vertices    (create-points (subvec raw-data (+ num-patches 2)))]
      (->>
        patches
        (map (partial create-surface divisions vertices))
        (reduce sp/augment)
        ; TODO: create polygons
        )))

(comment

  (load-shape "resources/newell-teapot/teapot" 16)

)
