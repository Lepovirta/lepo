(ns lepo.author-test
  (:require [lepo.author :as author]
            [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

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
  (is (= {:name "John Smith"
          :uri "/team/jsmith/"
          :full-uri "/team/jsmith/index.html"}
         (author/expand-details "jsmith" {:name "John Smith"}))))

(def author-gen
  (gen/fmap (fn [[id fname lname]]
              (author/expand-details id {:firstname fname :lastname lname}))
            (gen/tuple gen/keyword gen/string gen/string)))

(def sample-authors
  (list {:name "Adam Baldwin"}
        {:name "Bob Hoskins"}
        {:name "Charlie Sheen"}
        {:name "David Tennant"}))

(deftest author-sorting
  (dotimes [_ 20]
    (is (= sample-authors
           (author/sorted-authors (shuffle sample-authors))))))

(defspec sorting-authors-is-idempotent
  20
  (prop/for-all [authors (gen/vector author-gen 20)]
                (= (author/sorted-authors authors)
                   (author/sorted-authors (author/sorted-authors authors)))))
