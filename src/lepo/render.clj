(ns lepo.render
  (:require [selmer.parser :as selmer]
            [selmer.filters :refer [add-filter!]]
            [lepo.page :as page]
            [lepo.utils :as utils]
            [clj-time.core :as t]
            [clojure.java.io :as io]))

(defn init-filters! []
  (add-filter! :tag-uri page/tag-uri)
  (add-filter! :author-fullname page/author-fullname)
  (add-filter! :author-uri page/author-uri))

(defn- template-path
  [template-name]
  (.getPath (io/file "templates" (str template-name ".html"))))

(defn- template-from-page
  [page]
  (or (:template page) (-> page :page-type name)))

(defn- render
  [template-name conf]
  (selmer/render-file (template-path template-name) conf))

(defn page
  [conf page]
  (render (template-from-page page) (assoc conf :page page)))

(defn- filter-by-tag
  [posts tag]
  (filter (partial page/has-tag? tag) posts))

(defn tag
  [conf tag]
  (render "tag"
          (assoc conf
                 :name tag
                 :posts (filter-by-tag (:all-posts conf) tag))))

(defn- post-year [post] (t/year (:date post)))

(defn- to-group
  [[group posts]]
  {:group group :posts posts})

(defn- group-posts
  [posts]
  (->> posts
       (group-by post-year)
       (utils/reverse-sort-by first)
       (map to-group)))

(defn archives
  [conf]
  (render "archives" (assoc conf :groups (group-posts (:all-posts conf)))))

