(ns lepo.parse-test
  (:require [lepo.parse :as parse]
            [lepo.example-data :as example-data]
            [clojure.test :refer :all]
            [clj-time.core :as t]
            [lepo.test-utils :refer :all]))

(def expected-parsed-pages
  [{:uri "/posts/2015-12-01-hello-world.html"
    :path "/posts/2015-12-01-hello-world.html"
    :id "posts:2015-12-01-hello-world"
    :date (t/date-time 2015 12 1)
    :page-type :post
    :author-id :jsmith
    :description "cool story"
    :title "Hello World!"
    :tags ["mytag"]
    :content (list [:p {} "Hello World!"])}
   {:uri "/posts/2016-01-01-new-year.html"
    :path "/posts/2016-01-01-new-year.html"
    :id "posts:2016-01-01-new-year"
    :date (t/date-time 2016 1 1)
    :page-type :post
    :author-id :jdoe
    :description "yay!"
    :title "New Year!"
    :tags []
    :content (list [:p {} "Happy New Year!"])}
   {:uri "/posts/2016-06-15-summer-time.html"
    :path "/posts/2016-06-15-summer-time.html"
    :id "posts:2016-06-15-summer-time"
    :date (t/date-time 2016 6 15)
    :page-type :post
    :author-id :jdoe
    :title "Summer time!"
    :tags ["party"]
    :content (list [:p {} "so hot"])}
   {:uri "/team/jsmith/index.html"
    :path "/team/jsmith/index.html"
    :id "team:jsmith:index"
    :date nil
    :page-type :author
    :description "my homepage"
    :content (list [:p {} "Hi, I'm John Smith!"])}
   {:uri "/zombo.html"
    :path "/zombo.html"
    :id "zombo"
    :date nil
    :title "ZOMBOCOM"
    :page-type :normal
    :description "Welcome to ZOMBOCOM"
    :template "zombo"
    :content (list [:p {} "You can do anything in ZOMBOCOM!"])}])

(deftest page-parsing
  (table-test expected-parsed-pages
              (parse/parse-pages example-data/pages)))
