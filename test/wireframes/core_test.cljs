(ns wireframes.core-test
  (:use-macros [cljs-test.macros :only [deftest is= is]])
  (:require [cljs-test.core :as test]
            [inkspot.color :refer [coerce]]))

(deftest bootstrap
  (is= (+ 2 2) 4))
