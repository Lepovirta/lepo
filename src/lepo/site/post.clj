(ns lepo.site.post
  (:require [lepo.utils :as utils]))

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
  (let [all-tags       (:tags conf)
        author-id      (:author-id post)
        author-details (get-in conf [:authors author-id])]
    (assoc post
           :tags (map all-tags (:tags post))
           :author author-details)))

(defn augment
  [conf posts]
  (->> posts
       (map (partial augment-post conf))
       (utils/reverse-sort-by :date)
       link-posts))
