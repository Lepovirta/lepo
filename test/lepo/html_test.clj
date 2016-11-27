(ns lepo.html-test
  (:require [lepo.html :as html]
            [lepo.test-utils :refer :all]
            [clojure.test :refer :all]))

(def html-without-root
  [[:body {}
    [:p {}
     "Hello"
     [:a {:href "/world.html"} "world"]
     "!"]]
   [:p {}
    [:img {:src "myimg.jpg"}]
    [:img {:src "/base/myimg.jpg"}]
    [:img {:src "relative/myimg.jpg"}]
    [:img {:src "http://example.org/someimg.jpg"}]
    [:img {:src "//example.org/someimg.jpg"}]]
   [:a {:href "/index.html"}
    [:img {:src "/logo.png"}]]])

(def html-with-root
  [[:body {}
   [:p {}
    "Hello"
    [:a {:href "/someroot/world.html"} "world"]
    "!"]]
   [:p {}
    [:img {:src "myimg.jpg"}]
    [:img {:src "/someroot/base/myimg.jpg"}]
    [:img {:src "relative/myimg.jpg"}]
    [:img {:src "http://example.org/someimg.jpg"}]
    [:img {:src "//example.org/someimg.jpg"}]]
   [:a {:href "/someroot/index.html"}
    [:img {:src "/someroot/logo.png"}]]])

(defn add-root-path
  [root]
  (map (partial html/add-root-path root)
       html-without-root))

(deftest adding-root
  (table-test html-without-root (add-root-path ""))
  (table-test html-with-root (add-root-path "someroot")))
