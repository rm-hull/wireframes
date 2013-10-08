(ns wireframes.shape-loader
  (:refer-clojure :exclude [identity concat])
  (:use [wireframes.transform]
        [wireframes.shape-primitives]
        [wireframes.bezier-patch]
        [clojure.string :only [split-lines split]]))

(defn- parse-int [s]
  (Integer/parseInt s))

(defn- parse-double [s]
  (Double/parseDouble s))

(defn- parse-csv [converter line]
  (->>
    (split line #",")
    (map converter)))

(defn- create-points [vertices-data]
  (mapv (comp vec (partial parse-csv parse-double)) vertices-data))

(defn create-patches [patch-data]
  (let [f (partial parse-csv (comp dec parse-int))] ; teapot indexes start at 1...
    (->>                                            ; decrement for zero-offset indexing
      patch-data
      (map f))))

(defn load-shape [file]
  (let [raw-data    (vec (split-lines (slurp file)))
        num-patches (Integer/parseInt (raw-data 0))]
    {:patches (create-patches (subvec raw-data 1 (inc num-patches)))
     :vertices (create-points (subvec raw-data (+ num-patches 2))) }))

(comment

  (def patches (:patches (load-shape "resources/newell-teapot/teapot")))
  (def vertices (:vertices (load-shape "resources/newell-teapot/teapot")))
  (def control-points (compute-control-points (first patches) vertices))

  (evaluate-bezier-patch control-points 0 0)


)
