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

(defn get-author-details
  [conf author-id]
  (get-in conf [:authors author-id]))

(defn get-posts [site] (-> site :pages :post))

(defn- get-author-posts
  [posts author-id]
  (filter #(= author-id (:author %)) posts))

(defn- augment-post
  [conf post]
  (let [date (utils/str->date (:id post))
        author-id (:author post)
        author-details (get-author-details conf author-id)]
    (assoc post
           :date date
           :author-details author-details)))

(defn- augment-posts
  [conf posts]
  (->> posts
       (map (partial augment-post conf))
       (utils/reverse-sort-by :date)
       link-posts))

(defn- augment-author-page
  [conf posts page]
  (let [author-id (page/path->author-id (:id page))
        author-details (get-author-details conf author-id)
        title (or (:title page) (page/author-fullname author-details))
        author-posts (get-author-posts posts author-id)]
    (assoc page
           :title title
           :author author-id
           :author-posts author-posts
           :author-details author-details)))

(defn- augment-author-pages
  [conf posts pages]
  (map (partial augment-author-page conf posts) pages))

(defn- group-by-page-type
  [pages]
  (group-by :page-type pages))

(defn- process-pages
  [conf raw-pages]
  (let [pages (group-by-page-type raw-pages)
        posts (augment-posts conf (:post pages))
        author-pages (augment-author-pages conf posts (:author pages))]
    (assoc pages
           :post posts
           :author author-pages)))

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

