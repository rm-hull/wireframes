(ns wireframes.transform-tests
  (:refer-clojure :exclude [identity vec])
  (:use [clojure.test]
        [wireframes.transform]))

(def ε 0.000000001)

(defn =approx [^double a ^double b]
  (> ε (Math/abs (- a b))))

(defn eager-and [a b]
  (if b a false))

(defn =vector [expected actual]
  (is (reduce eager-and true
        (map
          =approx
          (clojure.core/vec (into-array Double/TYPE expected))
          (clojure.core/vec actual)))))

(defn =matrix [expected actual]
  (is
    (reduce eager-and true
      (map
        =vector
        expected actual))))

(deftest transpose-matrix
  (=matrix
    (matrix
        [ 1  5  9 13]
        [ 2  6 10 14]
        [ 3  7 11 15]
        [ 4  8 12 16])
    (transpose
      (matrix
        [ 1  2  3  4]
        [ 5  6  7  8]
        [ 9 10 11 12]
        [13 14 15 16]))))

(deftest combine-matrices
  (=matrix
    identity
    (combine
      identity
      identity))

  (=matrix
    (translate 4 6 9)
    (combine
      identity
      (translate 4 6 9)))

  (=matrix
    (translate 8 10 21)
    (combine
      (translate 5 3 2)
      (translate 3 7 19)))

  (=matrix
    (translate 11 12 19)
    (combine
      (translate 3 5 6)
      (translate 5 4 3)
      (translate 3 3 10))))

; TODO: rotate test --> combine is not associative

(deftest translate-point
  (=vector [8 10 13 1]
           ((transform-point (translate 3 5 6)) (point 5 5 7))))

(deftest scale-point
  (=vector [10 15 28 1]
           ((transform-point (scale 2 3 4)) (point 5 5 7))))

(deftest rotate-point
  (=vector [3 -9 6 1]
           ((transform-point (rotate :x (degrees->radians 90))) (point 3 6 9)))

  (=vector [9 6 -3 1]
           ((transform-point (rotate :y (degrees->radians 90))) (point 3 6 9)))

  (=vector [-6 3 9 1]
           ((transform-point (rotate :z (degrees->radians 90))) (point 3 6 9))))

(deftest perspective-point
  (=vector [22.909090909090907 11.454545454545453]
               ((perspective 20) (point 12.6 6.3 9))))

(deftest normal-3d-triangle
  (=vector [0 -32 0]
           (normal
             (point 3 5 6)
             (point 7 5 11)
             (point 3 5 14))))

(deftest triangulation
  (is (= [{:vertices [1 2 3] :color :red}] (triangulate {:vertices [1 2 3] :color :red})))
  (is (= [{:vertices [1 2 3] :color :green}
          {:vertices [1 3 4] :color :green}
          {:vertices [1 4 5] :color :green}]
         (triangulate {:vertices [1 2 3 4 5] :color :green})))
  (is (= [{:vertices nil}]   (triangulate {:vertices nil})))
  (is (= [{:vertices []}]    (triangulate {:vertices []})))
  (is (= [{:vertices [1]}]   (triangulate {:vertices [1]})))
  (is (= [{:vertices [1 2]}] (triangulate {:vertices [1 2]}))))

(deftest reduce-polys
  (is (= [{:vertices [1 2 3] :color :yellow}
          {:vertices [1 3 4] :color :yellow}
          {:vertices [2 3 5] :color :grey}
          {:vertices [2 5 6] :color :grey}
          {:vertices [2 6 7] :color :grey}]
         (reduce-polygons [{:vertices [1 2 3 4] :color :yellow}
                           {:vertices [2 3 5 6 7] :color :grey}]))))