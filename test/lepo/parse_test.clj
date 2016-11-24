(ns lepo.parse-test
  (:require [lepo.parse :as parse]
            [lepo.example-data :as example-data]
            [clojure.test :refer :all]
            [lepo.test-utils :refer :all]))

(def expected-parsed-pages
  [{:uri "/posts/2015-12-01-hello-world.html"
    :id "posts/2015-12-01-hello-world"
    :page-type :post
    :author-id :jsmith
    :description "cool story"
    :title "Hello World!"
    :tags ["mytag"]
    :content "<p>Hello World!</p>"}
   {:uri "/posts/2016-01-01-new-year.html"
    :id "posts/2016-01-01-new-year"
    :page-type :post
    :author-id :jdoe
    :description "yay!"
    :title "New Year!"
    :tags []
    :content "<p>Happy New Year!</p>"}
   {:uri "/posts/2016-06-15-summer-time.html"
    :id "posts/2016-06-15-summer-time"
    :page-type :post
    :author-id :jdoe
    :title "Summer time!"
    :tags ["party"]
    :content "<p>so hot</p>"}
   {:uri "/team/jsmith/index.html"
    :id "team/jsmith/index"
    :page-type :author
    :description "my homepage"
    :content "<p>Hi, I'm John Smith!</p>"}
   {:uri "/zombo.html"
    :id "zombo"
    :title "ZOMBOCOM"
    :page-type :normal
    :description "Welcome to ZOMBOCOM"
    :template "zombo"
    :content "<p>You can do anything in ZOMBOCOM!</p>"}])

(deftest page-parsing
  (table-test expected-parsed-pages
              (parse/parse-pages example-data/pages)))
