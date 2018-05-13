(ns lepo.site.archive
  (:require [lepo.utils :as utils]
            [java-time]))

(defn- pair->archive
  [[name posts]]
  {:name name :posts posts})

(defn- post-year
  [post]
  (java-time/year (:date post)))

(defn- group-by-year
  [pages]
  (->> pages
       (group-by post-year)
       (utils/reverse-sort-by first)))

(defn conf->archive
  [conf]
  (->> (get-in conf [:pages :post])
       group-by-year
       (map pair->archive)))

(defn latest-posts
  [conf]
  (take (get conf :latest-posts-count 5)
        (get-in conf [:pages :post])))
