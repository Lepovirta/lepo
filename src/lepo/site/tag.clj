(ns lepo.site.tag
  (:require [lepo.uri]
            [lepo.site.conf]))

(defn- has-tag?
  [tag post]
  (->> post
       :tags
       (map :tag)
       (some #{tag})))

(defn- filter-pages-by-tag
  [tag pages]
  (filter (partial has-tag? tag)
          pages))

(defn- posts-for-tag
  [pages tag]
  (->> (:post pages)
       (filter-pages-by-tag tag)))

(defn- tag-page
  [pages {uri :uri tag :tag}]
  {:uri       uri
   :page-type :tag
   :template  :tag
   :title     (str "Posts tagged &quot;" tag "&quot;")
   :posts     (posts-for-tag pages tag)})

(defn tag-pages
  [{tags :tags} pages]
  (->> (vals tags)
       (map (partial tag-page pages))))

(defn- pages->tags
  [pages]
  (->> pages
       (mapcat :tags)
       distinct
       sort))

(defn- uri
  [tag]
  (lepo.uri/parts->path lepo.site.conf/tags-path
                        (str tag ".html")))

(defn- expand-tag
  [tag]
  [tag
   {:uri (uri tag)
    :tag tag}])

(defn all-tags
  [conf pages]
  (into {}
        (map expand-tag
             (pages->tags pages))))
