(ns lepo.uri-test
  (:require [lepo.uri :as uri]
            [clojure.test :refer :all]))

(deftest from-parts
  (is (= "https://lepovirta.org/foo/bar/zap"
         (uri/parts->url "https://lepovirta.org/foo/" "//bar/" "zap//")))
  (is (= "/foo/bar/zap"
         (uri/parts->path "/foo/" "//bar/" "zap//")))
  (is (= "/foo/bar/zap/"
         (uri/parts->dir "/foo/" "//bar/" "zap//"))))

(deftest to-parts
  (is (= '("foo" "bar" "zap")
         (uri/path->parts "/foo/bar/zap/"))))

(deftest extension-handling
  (is (= "index"
         (uri/remove-extension "index.html")))
  (is (= "/index"
         (uri/remove-extension "/index.html")))
  (is (= "/dir/myimg"
         (uri/remove-extension "/dir/myimg.jpg"))))
