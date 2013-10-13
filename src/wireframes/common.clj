(ns wireframes.common
  (:require [clojure.string :as str]))

(defn parse-int [s]
  (when-not (empty? s)
    (Integer/parseInt s)))

(defn parse-double [s]
  (when-not (empty? s)
    (Double/parseDouble s)))

(defn parse-string [regex element-converter s]
  (->>
    (str/split s regex)
    (mapv element-converter)))

(def parse-csv (partial parse-string #","))

