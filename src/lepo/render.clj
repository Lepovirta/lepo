(ns lepo.render
  (:require [selmer.parser :as selmer]
            [selmer.filters :refer [add-filter!]]
            [lepo.rss :as rss]
            [lepo.page :as page]
            [lepo.site :as site]
            [lepo.utils :as utils]
            [clojure.java.io :as io]))

(def template-dirname "templates")

(defn init-filters! []
  (add-filter! :tag-uri page/tag-uri)
  (add-filter! :author-fullname page/post-author-fullname)
  (add-filter! :author-uri page/author-uri))

(defn- template-path
  [template-name]
  (.getPath (io/file template-dirname
                     (str template-name ".html"))))

(defn- template-from-page
  [page]
  (or (:template page) (-> page :page-type name)))

(defn- render
  [template-name conf]
  (selmer/render-file (template-path template-name) conf))

(defn page->html
  [conf page]
  (render (template-from-page page)
          (assoc conf :page page)))

(defn tag->html
  [conf tag]
  (render "tag"
          (assoc conf
                 :name tag
                 :posts (site/posts-for-tag conf tag))))

(defn archives->html
  [conf]
  (render "archives"
          (assoc conf :groups (site/posts-by-year conf))))

(defn- page-pair
  [conf page]
  [(:uri page) (fn [_] (page->html conf page))])

(defn- page-pairs
  [conf pages]
  (->> pages
       (map (partial page-pair conf))
       (into {})))

(defn pages
  [conf]
  (->> (:pages conf)
       (utils/map-values (partial page-pairs conf))
       (into {})))

(defn- tag-pair
  [conf tag]
  [(page/tag-uri tag) (fn [_] (tag->html conf tag))])

(defn tags
  [conf]
  (->> (:tags conf)
       (map (partial tag-pair conf))
       (into {})))

(defn archive
  [conf]
  {page/archive-uri (fn [_] (archives->html conf))})

(defn rss
  [conf]
  {(:atom-uri conf) (fn [_] (rss/atom-xml conf))})
