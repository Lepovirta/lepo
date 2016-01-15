(ns lepo.site
  (:require [clojure.string :as string]
            [lepo.utils :as utils]
            [lepo.page :as page]))

(defn- link-post
  ([prev current next]
    (assoc current :prev prev :next next))
  ([prev current]
   (link-post prev current nil)))

(defn- link-posts
  [posts]
  (let [first-post (link-post nil (first posts) (second posts))
        linked-posts (map (partial apply link-post) (partition 3 1 [] posts))]
    (cons first-post linked-posts)))

(defn- process-posts
  [conf all-pages pages]
  (->> pages
       (utils/reverse-sort-by :date)
       link-posts))

(defn- process-author-pages
  [conf all-pages pages]
  pages)

(def processes-by-page-type
  {:post process-posts
   :author process-author-pages})

(defn- process-by-page-type
  [conf all-pages page-type pages]
  (if-let [process (get processes-by-page-type page-type)]
    (process conf all-pages pages)
    pages))

(defn- group-by-page-type
  [pages]
  (group-by :page-type pages))

(defn- map-key-value
  [f coll]
  (map (fn [[k v]] [k (f k v)])
       coll))

(defn- process-pages
  [conf pages]
  (->> pages
       group-by-page-type
       (map-key-value (partial process-by-page-type conf pages))
       (into {})))

(defn get-posts [site] (-> site :pages :post))

(defn- latest-posts
  [site]
  (take (get site :latest-posts-count 5)
        (get-posts site)))

(defn- get-tags
  [site]
  (->> (get-posts site)
       (mapcat :tags)
       distinct
       sort))

(defn posts-for-tag
  [site tag]
  (->> site
       get-posts
       (filter (partial page/has-tag? tag))))

(defn- to-group
  [[group posts]]
  {:group group :posts posts})

(defn posts-by-year
  [site]
  (->> site
       get-posts
       (group-by page/post-year)
       (utils/reverse-sort-by first)
       (map to-group)))

(defn build
  [conf raw-pages]
  (let [pages (process-pages conf raw-pages)
        site (assoc conf :pages pages)]
    (assoc site
           :latest-posts (latest-posts site)
           :tags (get-tags site))))

