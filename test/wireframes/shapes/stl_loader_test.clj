(ns wireframes.shapes.stl-loader-test
  (:use [clojure.test]
        [wireframes.shapes.stl-loader]
        [wireframes.transform-tests])
  (:require [wireframes.transform :as t]))

(deftest convert-points
  (is (=matrix [(t/point 1 2 3) (t/point 4 5 6) (t/point 7 8 9)]
               (convert [[1 2 3] [4 5 6] [7 8 9]]))))

(deftest unpack-test
  (is (= {:normal [0 1 2]
          :points [[3 4 5] [6 7 8] [9 10 11]]
          :attributes 12}
         (unpack (range 13)))))

(deftest pack-test
  (is (= (range 13)
         (pack {:normal [0 1 2]
                :points [[3 4 5] [6 7 8] [9 10 11]]
                :attributes 12}))))

(deftest loader-test
  (let [shape (load-shape "resources/RichRap_Raspbery_Pi_Case_Bottom.stl")]
    (is (= 7644 (count (:points shape))))
    (is (= 2548 (count (:polygons shape))))))
