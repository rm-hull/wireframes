(ns wireframes.shapes.primitives-test
  (:use [clojure.test]
        [wireframes.shapes.primitives])
  (:require [wireframes.transform :as t]))

(defn =points [expected actual]
  (is (= (mapv #(vec (into-array Double/TYPE %)) expected)
         (mapv vec (:points actual)))))

(deftest make-point-test
  (is (=points [[1 2 3 1]]
               (make-point 1 2 3))))

(deftest make-line-test
  (is (=points [[0 0 0 1] [1 1 0 1] [2 2 0 1] [3 3 0 1] [4 4 0 1]]
               (apply make-line
                 (for [i (range 5)]
                   (make-point i i 0))))))

(deftest extrude-test
  (let [line (apply make-line
               (for [i (range 5)]
                 (make-point i i 0)))]
    (is (= [[0 1 6 5] [1 2 7 6] [2 3 8 7] [3 4 9 8] [5 6 11 10] [6 7 12 11] [7 8 13 12] [8 9 14 13] [10 11 16 15] [11 12 17 16] [12 13 18 17] [13 14 19 18]]
           (:polygons (extrude line (t/translate 0 0 1) 3))))))
