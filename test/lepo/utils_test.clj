(ns lepo.utils-test
  (:require [lepo.utils :as utils]
            [java-time]
            [clojure.test :refer :all]))

(deftest slash-handling
  (is (= "foo/bar/zap"
         (utils/trim-slashes "///foo/bar/zap//"))))

(deftest mapping-values
  (is (= '([:foo "foo!"] [:bar "bar!"] [:zap "zap!"])
         (utils/map-values #(str % "!")
                           {:foo "foo" :bar "bar" :zap "zap"}))))

(deftest reverse-sorting
  (is (= '(3 2 1)
         (utils/reverse-sort-by identity '(1 2 3)))))

(deftest date-parsing
  (is (= (java-time/local-date 2016 12 9)
         (utils/str->date "/posts/2016-12-09-hello-world.html")))
  (is (= (java-time/local-date 2011 8 12)
         (utils/str->date "posts:2011-08-12-hello-world"))))
