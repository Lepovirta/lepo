(ns lepo.site.author
  (require [lepo.author :as author]
           [lepo.page :as page]
           [clojure.tools.logging :as log]))

(defn- augment-author
  [paths author-id author]
  (let [author   (author/expand-details author-id author)
        full-uri (:full-uri author)]
    (if (paths full-uri)
      author
      (do (log/debug "No page for author" author-id "available")
          (dissoc author :full-uri :uri)))))

(defn- author-paths
  [pages]
  (->> pages
       (filter (comp (partial = :author) :page-type))
       (map :path)
       (into #{})))

(defn augment-authors
  [conf authors pages]
  (let [paths (author-paths pages)]
    (->> authors
         (map (fn [[id author]]
                [id (augment-author paths id author)]))
         (into {}))))

(defn- augment-author-page
  [conf posts page]
  (let [author-id      (author/path->author-id (:path page))
        author-details (get-in conf [:authors author-id])
        title          (or (:title page) (:name author-details))
        author-posts   (page/filter-by-author author-id posts)]
    (assoc page
           :title title
           :author-id author-id
           :author-posts author-posts
           :author author-details)))

(defn augment-author-pages
  [conf {posts :post pages :author}]
  (map (partial augment-author-page conf posts)
       pages))
