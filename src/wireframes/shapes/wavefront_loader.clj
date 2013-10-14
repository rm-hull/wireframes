(ns wireframes.shapes.wavefront-loader
  (:require [clojure.string :as str]
            [clojure.core.rrb-vector :as fv]
            [wireframes.common :as c]
            [wireframes.transform :as t]))

(def vertex-matcher
  (let [converter (partial c/parse-string #" +" c/parse-double)]
    (fn [s] {:points (fv/vector (converter s))})))

(def face-matcher
  ; waveform indexes start at one, so decrement as we are indexing from zero
  (let [converter1 (partial c/parse-string #"/" (comp c/decrement-offset c/parse-int))
        converter2 (partial c/parse-string #" +" converter1)]
    (fn [s]
      (let [vertices (converter2 s)]
        ; discard any polygons with less than three faces
        (when (> (count vertices) 2)
          {:polygons (->> vertices (mapv first) fv/vector)})))))

(def directives
  [[#"^v +(.*)" vertex-matcher]
   [#"^f +(.*)" face-matcher]])

(defn parse-line [directives data]
  (try
    (loop [directives directives]
      (when directives
        (let [[regex matcher] (first directives)
               result (re-find regex data)]
          (if result
            (matcher (second result))
            (recur (next directives))))))
    (catch Exception e
      (throw (RuntimeException. (str "Failed to parse: " data) e)))))

(defn load-shape [file]
  (->>
    (slurp file)
    (str/split-lines)
    (map (partial parse-line directives))
    (remove nil?)
    (reduce
      (partial merge-with fv/catvec)
      {:points (fv/vector) :lines (fv/vector) :polygons (fv/vector)})))
