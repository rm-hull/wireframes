(ns wireframes.shapes.wavefront-loader
  (:require [clojure.string :as str]
            [wireframes.common :as c]
            [wireframes.transform :as t]))

(def vertex-matcher
  (let [converter (partial c/parse-string #" " c/parse-double)]
    (fn [s] {:points [(converter s)]})))

(def face-matcher
  ; waveform indexes start at one, so decrement as we are indexing from zero
  (let [converter1 (partial c/parse-string #"/" (comp dec c/parse-int))
        converter2 (partial c/parse-string #" " converter1)]
    (fn [s]
      (let [vertex-indexes (mapv first (converter2 s))]
        {:lines (->>  ; add 1st element onto the end to form a closed path
                  (conj vertex-indexes (first vertex-indexes))
                  (partition 2 1)
                  (mapv vec))
         :polygons (t/triangulate vertex-indexes)}))))

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
    (reduce (partial merge-with conj) {:points [] :lines [] :polygons []})))

;                               ^ conj ideal but too nested
;                                 concat slow and stack overflow
(comment

  (time (load-shape "resources/obj_IconA5.obj"))


)
