(ns wireframes.shape-loader
  (:require [clojure.string :as str]
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

(defn load-shape [file]
  (let [raw-data    (vec (str/split-lines (slurp file)))
        num-patches (Integer/parseInt (raw-data 0))]
    {:patches (create-patches (subvec raw-data 1 (inc num-patches)))
     :vertices (create-points (subvec raw-data (+ num-patches 2))) }))

(comment

  (def patches (:patches (load-shape "resources/newell-teapot/teapot")))
  (def vertices (:vertices (load-shape "resources/newell-teapot/teapot")))
  (def control-points (bp/compute-control-points (first patches) vertices))

  (bp/evaluate-bezier-patch control-points 0 0)


)
