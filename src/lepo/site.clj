(ns lepo.site
  (:require [clojure.string :as string]
            [lepo.utils :as utils]
            [lepo.uri :as uri]
            [lepo.author :as author]
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

(defn has-uri?
  [conf uri]
  (contains? (:uris conf) uri))

(defn- get-author-posts
  [posts author-id]
  (filter #(= author-id (:author-id %)) posts))

(defn- augment-common
  [conf page]
  (let [site-url  (:site-url conf)
        page-uri  (:uri page)
        url       (uri/parts->url site-url page-uri)
        page-type (:page-type page)
        og        {:type (page/page-type->og-type page-type)
                   :description (:description page)
                   :url url
                   :title (:title page)}]
    (assoc page
           :url url
           :og og)))

(defn- augment-common-many
  [conf pages]
  (map (partial augment-common conf)
       pages))

(defn- augment-post
  [conf post]
  (let [date (utils/str->date (:id post))
        author-id (:author-id post)
        author-details (get-author-details conf author-id)]
    (assoc post
           :date date
           :author author-details)))

(defn- augment-posts
  [conf posts]
  (->> posts
       (map (partial augment-post conf))
       (utils/reverse-sort-by :date)
       link-posts))

(defn- augment-author-page
  [conf posts page]
  (let [author-id (author/path->author-id (:id page))
        author-details (get-author-details conf author-id)
        title (or (:title page) (:fullname author-details))
        author-posts (get-author-posts posts author-id)]
    (assoc page
           :title title
           :author-id author-id
           :author-posts author-posts
           :author author-details)))

(defn- augment-author-pages
  [conf posts pages]
  (map (partial augment-author-page conf posts) pages))

(defn- group-by-page-type
  [pages]
  (group-by :page-type pages))

(defn- process-pages
  [conf raw-pages]
  (let [augmented-pages (augment-common-many conf raw-pages)
        pages (group-by-page-type augmented-pages)
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
  (filter (partial page/has-tag? tag)
          (get-posts site)))

(defn- to-group
  [[group posts]]
  {:group group :posts posts})

(defn posts-by-year
  [site]
  (->> (get-posts site)
       (group-by page/post-year)
       (utils/reverse-sort-by first)
       (map to-group)))

(defn- augment-project
  [conf project]
  (assoc project
         :github-url (str (:github conf) "/" (:github-id project))))

(defn- augment-projects
  [conf]
  (let [raw-projects (:projects conf)
        projects (map (partial augment-project conf) raw-projects)
        project-rows (partition 2 projects)]
    (assoc conf
           :projects projects
           :project-rows project-rows)))

(defn- augment-author
  [conf author-id author]
  (let [augmented (author/expand-details author-id author)
        full-uri (:full-uri augmented)]
    (if (has-uri? conf full-uri)
      augmented
      (dissoc augmented :full-uri :uri))))

(defn- augment-authors
  [conf]
  (->> conf
       :authors
       (map (fn [[id details]] [id (augment-author conf id details)]))
       (into {})
       (assoc conf :authors)))

(defn- pages->uris [pages] (into #{} (map :uri pages)))

(defn- add-uris
  [conf raw-pages]
  (assoc conf :uris (pages->uris raw-pages)))

(defn- add-pages
  [conf raw-pages]
  (assoc conf :pages (process-pages conf raw-pages)))

(defn- add-latest-posts
  [conf]
  (assoc conf :latest-posts (latest-posts conf)))

(defn- add-tags
  [conf]
  (assoc conf :tags (get-tags conf)))

(defn build
  [conf raw-pages]
  (-> conf
      (add-uris raw-pages)
      augment-authors
      (add-pages raw-pages)
      add-latest-posts
      augment-projects
      add-tags))
