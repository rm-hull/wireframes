(ns wireframes.shapes.wavefront-loader
  (:require [clojure.string :as str]
            [clojure.core.rrb-vector :as fv]
            [wireframes.common :as c]
            [wireframes.transform :as t]))

(def vertex-matcher
  (let [converter (partial c/parse-string #" " c/parse-double)]
    (fn [s] {:points (fv/vector (converter s))})))

(def face-matcher
  ; waveform indexes start at one, so decrement as we are indexing from zero
  (let [converter1 (partial c/parse-string #"/" (comp dec c/parse-int))
        converter2 (partial c/parse-string #" " converter1)]
    (fn [s]
      (let [vertex-indexes (mapv first (converter2 s))]
        {:lines (->>  ; add 1st element onto the end to form a closed path
                  (conj vertex-indexes (first vertex-indexes))
                  (partition 2 1)
                  (map vec)
                  (fv/vec))
         :polygons (fv/vec (t/triangulate vertex-indexes))}))))

(def directives
  [[#"^v (.*)" vertex-matcher]
   [#"^f (.*)" face-matcher]])

(defn parse-line [directives line]
  (loop [directives directives]
    (when directives
      (let [[regex matcher] (first directives)
             result (re-find regex line)]
        (if result
          (matcher (second result))
          (recur (next directives)))))))

(defn load-shape [file]
  (->>
    (slurp file)
    (str/split-lines)
    (map (partial parse-line directives))
    (remove nil?)
    (reduce
      (partial merge-with fv/catvec)
      {:points (fv/vector) :lines (fv/vector) :polygons (fv/vector)})))
