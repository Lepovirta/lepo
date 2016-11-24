(ns lepo.utils-test
  (:require [lepo.utils :as utils]
            [clojure.test :refer :all]))

(deftest slash-handling
  (is (= "foo/bar/zap"
         (utils/trim-slashes "///foo/bar/zap//"))))

(deftest uri-handling
  (is (= "foo/bar/zap"
         (utils/url "/foo/" "//bar/" "zap//")))
  (is (= "/foo/bar/zap"
         (utils/uri "/foo/" "//bar/" "zap//")))
  (is (= "/foo/bar/zap/"
         (utils/uri-dir "/foo/" "//bar/" "zap//"))))

(deftest mapping-values
  (is (= '([:foo "foo!"] [:bar "bar!"] [:zap "zap!"])
         (utils/map-values #(str % "!")
                           {:foo "foo" :bar "bar" :zap "zap"}))))

(deftest path-to-dir-list
  (is (= '("foo" "bar" "zap")
         (utils/dir-list "/foo/bar/zap/"))))

(deftest reverse-sorting
  (is (= '(3 2 1)
         (utils/reverse-sort-by identity '(1 2 3)))))
