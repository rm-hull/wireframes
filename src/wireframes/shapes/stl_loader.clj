(ns wireframes.shapes.stl-loader
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.core.rrb-vector :as fv]
            [wireframes.common :as c]
            [wireframes.transform :as t])
  (:use [gloss.core]
        [gloss.io :only [decode decode-all]]
        [byte-streams :only [to-byte-buffer]]))

(defcodec point-spec
  {:x :float32-le
   :y :float32-le
   :z :float32-le})

(defcodec triangle-spec
  {:normal point-spec
   :points [point-spec point-spec point-spec]
   :attribute :uint16-le})

(defcodec stl-spec
  {:header (string :ascii :length 80)
   :triangles (repeated triangle-spec :prefix :uint32-le)})

(defn convert-points [points]
  (fv/vec
    (for [{:keys [x y z]} points]
      (t/point x y z))))

(defn load-shape [file]
  (let [data (->> file io/file to-byte-buffer (decode stl-spec))
        points (->> (:triangles data) (mapcat (comp convert-points :points)) fv/vec)
        polygons (->>  (count points) range (partition 3) (mapv vec))]
    {:points  points
     :polygons polygons}))

(comment
(:polygons (load-shape  "resources/RichRap_Raspbery_Pi_Case_Bottom.stl")
))

