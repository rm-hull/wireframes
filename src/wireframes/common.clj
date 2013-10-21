(ns wireframes.common
  (:require [clojure.string :as str]))

(defn parse-int
  ([s] (parse-int s 10))
  ( [s r]
  (when-not (empty? s)
    ^{:cljs (js/parseInt s r)}
    (Integer/parseInt s r))))

(defn parse-double [s]
  (when-not (empty? s)
    ^{:cljs (js/parseFloat s)}
    (Double/parseDouble s)))

(defn decrement-offset [n]
  (when-not (nil? n)
    (dec n)))

(defn parse-string [regex element-converter s]
  (->>
    (str/split s regex)
    (mapv element-converter)))

(def parse-csv (partial parse-string #","))

(defn simple-concat
  "Useful for when (count xs) is small, cons's the xs onto the front of the ys
   without the overhead of lazy thunking. Disadvantage is that for large xs
   stack overflow may occur."
  [xs ys]
  (if (seq xs)
    (cons (first xs) (simple-concat (next xs) ys))
    ys))

(defn pad [s n]
  (->
    (apply str s (repeat n \space))
    (subs 0 n)))
