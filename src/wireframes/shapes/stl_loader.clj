(ns wireframes.shapes.stl-loader
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.core.rrb-vector :as fv]
            [wireframes.common :as c]
            [wireframes.transform :as t])
  (:use [gloss.core]
        [gloss.io :only [decode encode]]
        [byte-streams :only [to-byte-buffer print-bytes transfer]]))

(defcodec point-spec
  (ordered-map
    :x :float32-le
    :y :float32-le
    :z :float32-le))

(defcodec triangle-spec
  (ordered-map
    :normal point-spec
    :points [point-spec point-spec point-spec]
    :attributes :uint16-le))

(defcodec stl-spec
  (ordered-map
    :header (string :ascii :length 80)
    :triangles (repeated triangle-spec :prefix :uint32-le)))

(defn convert [points]
  (fv/vec
    (for [{:keys [x y z]} points]
      (t/point x y z))))

(defn load-shape [file]
  (let [data (->> file io/file to-byte-buffer (decode stl-spec))
        points (->> (:triangles data) (mapcat (comp convert :points)) fv/vec)
        polygons (->> (count points) range (partition 3) (mapv #(hash-map :vertices (vec %))))]
    {:points  points
     :polygons polygons}))

(defn build-triangle [polygon]
  {:normal (apply t/normal (:vertices polygon))
   :points (map t/vec (:vertices polygon))
   :attributes 0})

(defn save-shape [shape description file]
  (let [file     (io/file file)
        polygons (for [polygon (t/reduce-polygons (:polygons shape))]
                   (mapv (:points shape) polygon))]
    (.delete file)
    (transfer
      (encode stl-spec
        {:header (c/pad description 80)
         :triangles (map build-triangle polygons)})
      (io/file file))))