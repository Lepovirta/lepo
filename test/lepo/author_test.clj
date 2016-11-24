(ns lepo.author-test
  (:require [lepo.author :as author]
            [clojure.test :refer :all]))

(deftest fullname-from-author
  (is (= "Penn Jillette"
         (author/fullname {:firstname "Penn" :lastname "Jillette"})))
  (is (= "Teller"
         (author/fullname {:firstname "Teller"}))))

(deftest author-id-operations
  (is (= "/team/jkpl/"
         (author/uri "jkpl")))
  (is (= "/team/jkpl/index.html"
         (author/full-uri "jkpl")))
  (is (= :jkpl
         (author/path->author-id "/team/jkpl/index.html")))
  (is (= :jkpl
         (author/path->author-id "/team/jkpl/"))))

(deftest expanded-author-info
  (is (= {:firstname "John"
          :lastname "Smith"
          :fullname "John Smith"
          :uri "/team/jsmith/"
          :full-uri "/team/jsmith/index.html"}
         (author/expand-details "jsmith" {:firstname "John" :lastname "Smith"}))))
