(ns wireframes.common_tests
  (:use [clojure.test]
        [wireframes.common]))

(deftest simple-concat-test
  (is (empty? (simple-concat [] [])))
  (is (empty? (simple-concat nil nil)))
  (is (= [:a] (simple-concat [:a] nil)))
  (is (= [:a] (simple-concat nil [:a])))
  (is (= [:a :b] (simple-concat [:a] [:b])))
  (is (= [:a :b :c :d] (simple-concat [:a :b] [:c :d])))
  )
