(ns lepo.test-utils
  (:require [clojure.test :refer :all]))

(defn table-test
  ([expected-and-actual]
   (doseq [[expected actual] expected-and-actual]
     (is (= expected actual))))
  ([expected actual]
   (is (= (count expected) (count actual)) "There should be as my expected elements as actual elements.")
   (table-test (map vector expected actual))))
