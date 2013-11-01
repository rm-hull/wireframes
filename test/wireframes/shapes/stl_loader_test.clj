(ns wireframes.shapes.stl-loader-test
  (:use [clojure.test]
        [wireframes.shapes.stl-loader])
  (:require [wireframes.transform :as t]))


(deftest convert-points
  (is (= [(t/point 1 2 3) (t/point 4 5 6) (t/point 7 8 9)]
         (convert [{:x 1 :y 2 :z 3 } {:x 4 :y 5 :z 6} {:x 7 :y 8 :z 9}]))))

(deftest loader-test
  (let [shape (load-shape "data-files/RichRap_Raspbery_Pi_Case_Bottom.stl")]
    (is (= 7644 (count (:points shape))))
    (is (= 2548 (count (:polygons shape))))))
