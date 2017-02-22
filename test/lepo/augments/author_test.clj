(ns lepo.augments.author-test
  (:require [lepo.augments.author :as a]
            [clojure.test :refer :all]))

(def plain-authors
  {:mikes {:name "Mike Stoklasa"}
   :jayb  {:name "Jay Bauman"}
   :riche {:name "Rich Evans"}})

(def augmented-authors
  {:mikes {:name "Mike Stoklasa"
           :full-uri "/team/mikes/index.html"
           :uri "/team/mikes/"}
   :jayb  {:name "Jay Bauman"}
   :riche {:name "Rich Evans"
           :full-uri "/team/riche/index.html"
           :uri "/team/riche/"}})

(def sample-posts
  [{:page-type :post
    :uri "/post/2013-11-09-hitb.html"
    :author-id :mikes}
   {:page-type :post
    :uri "/post/2013-06-12-hitb.html"
    :author-id :mikes}
   {:page-type :post
    :uri "/post/2012-01-02-hitb.html"
    :author-id :mikes}
   {:page-type :post
    :uri "/post/2014-01-02-star-wars.html"
    :author-id :riche}
   {:page-type :post
    :uri "/post/2015-03-02-botw.html"
    :author-id :jayb}])

(def author-pages
  [{:page-type :author
    :path "/team/mikes/index.html"
    :content "asdf"}
   {:page-type :author
    :path "/team/riche/index.html"
    :title "I love Star Wars"
    :content "fdsa"}])

(def sample-pages
  (concat sample-posts
          author-pages
          [{:page-type :normal :uri "/index.html"}]))

(def full-author-pages
  [{:page-type :author
    :title "Mike Stoklasa"
    :author-id :mikes
    :content "asdf"
    :path "/team/mikes/index.html"
    :author {:name "Mike Stoklasa"
             :full-uri "/team/mikes/index.html"
             :uri "/team/mikes/"}
    :author-posts (list
                   {:page-type :post
                    :uri "/post/2013-11-09-hitb.html"
                    :author-id :mikes}
                   {:page-type :post
                    :uri "/post/2013-06-12-hitb.html"
                    :author-id :mikes}
                   {:page-type :post
                    :uri "/post/2012-01-02-hitb.html"
                    :author-id :mikes})}
   {:page-type :author
    :title "I love Star Wars"
    :author-id :riche
    :content "fdsa"
    :path "/team/riche/index.html"
    :author {:name "Rich Evans"
             :full-uri "/team/riche/index.html"
             :uri "/team/riche/"}
    :author-posts (list
                   {:page-type :post
                    :uri "/post/2014-01-02-star-wars.html"
                    :author-id :riche})}])

(deftest augment-authors
  (is (= augmented-authors
         (a/augment-authors {} plain-authors sample-pages))))

(deftest augment-author-pages
  (is (= full-author-pages
         (a/augment-author-pages
          {:authors augmented-authors}
          {:post sample-posts
           :author author-pages}))))
