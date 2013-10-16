(ns wireframes.common_tests
  (:use [clojure.test]
        [wireframes.common]))

(deftest simple-concat-test
  (is (empty? (simple-concat [] [])))
  (is (empty? (simple-concat nil nil)))
  (is (= [:a] (simple-concat [:a] nil)))
  (is (= [:a] (simple-concat nil [:a])))
  (is (= [:a :b] (simple-concat [:a] [:b])))
  (is (= [:a :b :c :d] (simple-concat [:a :b] [:c :d]))))

(deftest pad-test
  (is ( = 80 (count (pad nil 80))))
  (is ( = 80 (count (pad "" 80))))
  (is ( = 80 (count (pad "hello" 80))))
  (is ( = 80 (count (pad (apply str (repeat 400 "hello")) 80))))
  (is ( = "hello     " (pad "hello" 10)))
  (is ( = "hello worl" (pad "hello world!" 10))))
