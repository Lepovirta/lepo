(ns lepo.page-test
  (:require [lepo.page :as page]
            [clojure.test :refer :all]))

(def example-page
  {:title "foo"
   :tags ["sometag" "mytag"]
   :page-type :post})

(deftest tag-operations
  (is (= "/tags/mytag.html"
         (page/tag-uri "mytag")))
  (is (page/has-tag? "mytag" example-page))
  (is (not (page/has-tag? "mytag"
                          (assoc example-page :tags ["sometag"])))))

(deftest page-type-operations
  (is (page/post? example-page))
  (is (not (page/post? (assoc example-page :page-type :normal))))
  (is (= :normal
         (page/path->page-type "/index.html")))
  (is (= :author
         (page/path->page-type "/team/jkpl/")))
  (is (= :normal
         (page/path->page-type "/archive.html")))
  (is (= :post
         (page/path->page-type "/posts/2016-11-09-clickbaity-title.html")))
  (is (= "website"
         (page/page-type->og-type :normal)))
  (is (= "article"
         (page/page-type->og-type :post))))

(deftest page-id-operations
  (is (= "posts/2016-10-10-hello-world"
         (page/filename->page-id "/posts/2016-10-10-hello-world.html"))))
