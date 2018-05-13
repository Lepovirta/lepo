(ns lepo.site.root-path-test
  (:require [lepo.site.root-path :as root-path]
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

(def trees-without-root
  [{:root-path "root"
    :uri "/foo/bar"
    :asdf "/bar/foo"
    :asdf-uri "/zap"}
   {:uri "/foobar"}
   {:root-path "root"
    :v ["/foobar" {:uri "foobar"}]}
   {:root-path "foobar"
    :nodes [{:a "/asdf"
             :b (list [:uri "/yo"])}]}])

(def trees-with-root
  [{:root-path "root"
    :uri "/root/foo/bar"
    :asdf "/bar/foo"
    :asdf-uri "/root/zap"}
   {:uri "/foobar"}
   {:root-path "root"
    :v ["/foobar" {:uri "foobar"}]}
   {:root-path "foobar"
    :nodes [{:a "/asdf"
             :b (list [:uri "/foobar/yo"])}]}])

(defn add-root-path-to-html
  [root]
  (map (partial root-path/add-root-to-html root)
       html-without-root))

(deftest adding-root-to-html
  (table-test html-without-root
              (add-root-path-to-html ""))
  (table-test html-without-root
              (add-root-path-to-html "/"))
  (table-test html-with-root
              (add-root-path-to-html "someroot")))

(deftest adding-root-to-form
  (table-test trees-with-root
              (map root-path/add-root trees-without-root)))
