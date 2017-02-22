(ns lepo.augments.post
  (:require [lepo.utils :as utils]
            [lepo.page :as page]))

(defn- link-post
  [[prev current next]]
  (assoc current :prev prev :next next))

(defn- with-siblings
  [items]
  (let [[first second] items]
    (cons [nil first second]
          (partition 3 1 [] items))))

(defn- link-posts
  [posts]
  (map link-post (with-siblings posts)))

(defn- augment-post
  [conf post]
  (let [author-id      (:author-id post)
        author-details (get-in conf [:authors author-id])]
    (assoc post
           :author author-details)))

(defn augment
  [conf posts]
  (->> posts
       (map (partial augment-post conf))
       (utils/reverse-sort-by :date)
       link-posts))
