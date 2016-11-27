(ns lepo.uri-test
  (:require [lepo.uri :as uri]
            [clojure.test :refer :all]))

(deftest parts-to-uri
  (is (= "https://lepovirta.org/foo/bar/zap"
         (uri/parts->url "https://lepovirta.org/foo/" "//bar/" "zap//")))
  (is (= "/foo/bar/zap"
         (uri/parts->path "/foo/" "//bar/" "zap//")))
  (is (= "/foo/bar/zap/"
         (uri/parts->dir "/foo/" "//bar/" "zap//"))))

(deftest path-to-parts
  (is (= '("foo" "bar" "zap")
         (uri/path->parts "/foo/bar/zap/"))))

(deftest remove-extension
  (is (= "index"
         (uri/remove-extension "index.html")))
  (is (= "/index"
         (uri/remove-extension "/index.html")))
  (is (= "/dir/myimg"
         (uri/remove-extension "/dir/myimg.jpg"))))

(deftest absolute-path
  (is (uri/absolute-path? "/index.html"))
  (is (uri/absolute-path? "/dir/index.html"))
  (is (not (uri/absolute-path? "http://example.org/index.html")))
  (is (not (uri/absolute-path? "//example.org/index.html")))
  (is (not (uri/absolute-path? "index.html")))
  (is (not (uri/absolute-path? "dir/index.html"))))

(deftest add-root-path
  (is (= "/index.html" (uri/add-root-path "" "/index.html")))
  (is (= "/dir/index.html" (uri/add-root-path "dir" "/index.html")))
  (is (= "/dir/img/pic.jpg" (uri/add-root-path "dir" "/img/pic.jpg")))
  (is (= "img/pic.jpg" (uri/add-root-path "dir" "img/pic.jpg")))
  (is (= "stuff.html" (uri/add-root-path "dir" "stuff.html")))
  (is (= "//example.org/index.html" (uri/add-root-path "dir" "//example.org/index.html")))
  (is (= "http://example.org/index.html" (uri/add-root-path "dir" "http://example.org/index.html"))))
