(ns lepo.parse-test
  (:require [lepo.parse :as parse]
            [lepo.example-data :as example-data]
            [clojure.test :refer :all]
            [lepo.test-utils :refer :all]))

(def expected-parsed-pages
  [{:uri "/posts/2015-12-01-hello-world.html"
    :path "/posts/2015-12-01-hello-world.html"
    :author-id :jsmith
    :description "cool story"
    :title "Hello World!"
    :tags ["mytag"]
    :content (list [:p {} "Hello World!"])}
   {:uri "/posts/2016-01-01-new-year.html"
    :path "/posts/2016-01-01-new-year.html"
    :author-id :jdoe
    :description "yay!"
    :title "New Year!"
    :tags []
    :content (list [:p {} "Happy New Year!"])}
   {:uri "/posts/2016-06-15-summer-time.html"
    :path "/posts/2016-06-15-summer-time.html"
    :author-id :jdoe
    :title "Summer time!"
    :tags ["party"]
    :content (list [:p {} "so hot"])}
   {:uri "/team/jsmith/index.html"
    :path "/team/jsmith/index.html"
    :description "my homepage"
    :content (list [:p {} "Hi, I'm John Smith!"])}
   {:uri "/zombo.html"
    :path "/zombo.html"
    :title "ZOMBOCOM"
    :description "Welcome to ZOMBOCOM"
    :template "zombo"
    :content (list [:p {} "You can do anything in ZOMBOCOM!"])}])

(deftest page-parsing
  (table-test expected-parsed-pages
              (parse/parse-pages example-data/pages)))
