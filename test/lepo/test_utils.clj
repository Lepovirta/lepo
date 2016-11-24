(ns lepo.test-utils
  (:require [clojure.test :refer :all]))

(defn table-test
  ([expected-and-actual]
   (doseq [[expected actual] expected-and-actual]
     (is (= expected actual))))
  ([expected actual]
   (table-test (map vector expected actual))))
