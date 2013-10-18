(ns wireframes.shapes.stl-loader
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.core.rrb-vector :as fv]
            [wireframes.common :as c]
            [wireframes.transform :as t])
  (:use [gloss.core]
        [gloss.io :only [decode encode]]
        [byte-streams :only [to-byte-buffer print-bytes transfer]]))

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

(defn convert [points]
  (fv/vec
    (for [[x y z] points]
      (t/point x y z))))

(defn unpack
  "Unpacks thirteen data points, comprising 3xNormal, 9xVertices, 1xAttribute into a map
   structure - this in theory should be unnecessary if not for gloss codecs working as
   expected."
  [[norm-x norm-y norm-z
    point1-x point1-y point1-z
    point2-x point2-y point2-z
    point3-x point3-y point3-z
    attributes]]
  {:normal [norm-x norm-y norm-z]
   :points [[point1-x point1-y point1-z]
            [point2-x point2-y point2-z]
            [point3-x point3-y point3-z]]
   :attributes attributes})

(defn pack [{:keys [normal points attributes]}]
  (flatten (concat normal points [attributes])))

(defn load-shape [file]
  (let [data (->> file io/file to-byte-buffer (decode stl-spec))
        points (->> (:triangles data) (mapcat (comp convert :points unpack)) fv/vec)
        polygons (->>  (count points) range (partition 3) (mapv vec))]
    {:points  points
     :polygons polygons}))

(defn build-triangle [polygon]
  {:normal (apply t/normal polygon)
   :points (map t/vec polygon)
   :attributes 0})

(defn save-shape [shape description file]
  (let [file     (io/file file)
        polygons (for [polygon (t/reduce-polygons (:polygons shape))]
                   (mapv (:points shape) polygon))]
    (.delete file)
    (transfer
      (encode stl-spec
        {:header (c/pad description 80)
         :triangles (map (comp pack build-triangle) polygons)})
      (io/file file))))
