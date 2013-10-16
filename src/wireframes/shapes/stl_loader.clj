(ns wireframes.shapes.stl-loader
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.core.rrb-vector :as fv]
            [wireframes.common :as c]
            [wireframes.transform :as t])
  (:use [gloss.core]
        [gloss.io :only [decode encode]]
        [byte-streams :only [to-byte-buffer print-bytes]]))

;(defcodec point-spec
;  {:x :float32-le
;   :y :float32-le
;   :z :float32-le})
;
;(defcodec triangle-spec
;  {:normal point-spec
;   :points [point-spec point-spec point-spec]
;   :attributes :uint16-le})
;
;(defcodec stl-spec
;  {:header (string :ascii :length 80)
;   :triangles (repeated triangle-spec :prefix :uint32-le)})

(def point-spec
  [:float32-le :float32-le :float32-le])

(def triangle-spec
  (concat
    point-spec      ; normal
    point-spec      ; vertex 1
    point-spec      ; vertex 2
    point-spec      ; vertex 3
    [:uint16-le]))  ; attributes

(defcodec stl-spec
  {:header (string :ascii :length 80)
   :triangles (repeated triangle-spec :prefix :uint32-le)})

(defn- convert-points [points]
  (fv/vec
    (for [[x y z] points]
      (t/point x y z))))

(defn- pluck-points
  "Thirteen data points, comprising 3xNormal, 9xVertices, 1xAttribute.
   We are only concerned with scooping the vertices."
  [data]
  (->>
    data
    (drop 3)
    (take 9)
    (partition 3)))

(defn load-shape [file]
  (let [data (->> file io/file to-byte-buffer (decode stl-spec))
        points (->> (:triangles data) (mapcat (comp convert-points pluck-points)) fv/vec)
        polygons (->>  (count points) range (partition 3) (mapv vec))]
    {:points  points
     :polygons polygons}))
