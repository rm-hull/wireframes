(ns wireframes.transform-tests
  (:refer-clojure :exclude [identity concat])
  (:use [clojure.test]
        [wireframes.transform]))

(deftest concat-identity-identity
  (is (= (identity)
         (concat
           (identity)
           (identity)))))

(deftest concat-identity-vector
  (is (= (translate 4 6 9)
         (concat
           (identity)
           (translate 4 6 9)))))

(deftest concat-two-vectors
  (is (= (translate 8 10 21)
         (concat
           (translate 5 3 2)
           (translate 3 7 19)))))
