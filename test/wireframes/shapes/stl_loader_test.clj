(ns wireframes.shapes.stl-loader-test
  (:require
    [clojure.test :refer :all]
    [me.raynes.fs :as fs]
    [wireframes.transform :as t]
    [wireframes.shapes.curved-solids :as cs]
    [wireframes.shapes.stl-loader :refer :all]))

(deftest convert-points
  (is (= [(t/point 1 2 3) (t/point 4 5 6) (t/point 7 8 9)]
          (convert [[1 2 3] [4 5 6] [7 8 9]]))))

(deftest load-test
  (let [shape (load-shape "data-files/RichRap_Raspbery_Pi_Case_Bottom.stl")]
    (is (= 7644 (count (:points shape))))
    (is (= 2548 (count (:polygons shape))))))

(deftest save-test
  (let [sphere (cs/make-isosphere 3 3)
        fname  (fs/temp-file "isosphere-test-" ".stl")
        p-count (save-shape sphere "Geodesic sphere" fname)]

    ; Binary STL stores points not refs, so the points count will be
    ; 3 times the triangulated number of polygons
    (is (= (count (:points (load-shape fname)))
           (* 3 p-count)))

    (is (= (count (:polygons (load-shape fname)))
           (count (:polygons sphere))))))

