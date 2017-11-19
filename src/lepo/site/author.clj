(ns lepo.site.author
  (require [lepo.site.conf]
           [lepo.uri]
           [clojure.string :as string]
           [clojure.tools.logging :as log]))

(defn- uri
  [author-id]
  (lepo.uri/parts->dir lepo.site.conf/author-path (name author-id)))

(defn- full-uri
  [author-id]
  (lepo.uri/parts->path (uri author-id) "index.html"))

(defn- path->author-id
  [path]
  (let [[main-dir author-id] (lepo.uri/path->parts path)]
    (when (= main-dir lepo.site.conf/author-path)
      (keyword author-id))))

(defn- augment-author
  [paths author-id author]
  (let [has-local-uri (paths (full-uri author-id))
        author-uri    (if has-local-uri
                        (uri author-id)
                        (:homepage author))]
    (if-not has-local-uri
      (log/debug "No page for author" author-id "available"))
    (assoc author :uri author-uri)))

(defn augment-authors
  [conf authors paths]
  (into {}
        (map (fn [[id author]]
               [id (augment-author paths id author)])
             authors)))

(defn- filter-by-author
  [author-id pages]
  (filter (comp (partial = author-id) :author-id)
          pages))

(defn- augment-author-page
  [conf posts page]
  (let [author-id      (path->author-id (:path page))
        author-details (get-in conf [:authors author-id])
        title          (or (:title page) (:name author-details))
        author-posts   (filter-by-author author-id posts)]
    (assoc page
           :title title
           :author-posts author-posts
           :author author-details)))

(defn augment-author-pages
  [conf {posts :post pages :author}]
  (map (partial augment-author-page conf posts)
       pages))
