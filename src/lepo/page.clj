(ns lepo.page
  (:require [lepo.utils :as utils]
            [lepo.uri :as uri]
            [lepo.author :as author]
            [clj-time.core :as t]
            [clojure.string :as string]))

(def posts-path "posts")
(def tags-path "tags")

(def page-type-dirs
  {posts-path :post
   author/author-path :author})

(def og-types
  {:post "article"})

(defn tag-uri
  [tag]
  (uri/parts->path tags-path (str tag ".html")))

(def tags-uri
  (uri/parts->path tags-path "index.html"))

(def archive-uri
  (uri/parts->path posts-path "index.html"))

(defn has-tag?
  [tag post]
  (some #{tag} (:tags post)))

(defn post?
  [page]
  (= (:page-type page) :post))

(defn post-year [post] (t/year (:date post)))

(defn filename->page-id
  [filename]
  (-> filename
      utils/trim-slashes
      uri/remove-extension
      (string/replace "/" ":")))

(defn path->page-type
  [path]
  (get page-type-dirs (first (uri/path->parts path)) :normal))

(defn page-type->og-type
  [page-type]
  (get og-types page-type "website"))

(defn page-url
  [url page]
  (uri/parts->url url (:uri page)))

(defn group-by-type
  [pages]
  (group-by :page-type pages))

(defn pages->uris
  [pages]
  (into #{} (map :uri pages)))

(defn pages->tags
  [pages]
  (->> pages
       (mapcat :tags)
       distinct
       sort))

(defn filter-by-author
  [author-id pages]
  (filter (comp (partial = author-id) :author-id)
          pages))

(defn filter-by-tag
  [tag pages]
  (filter (partial has-tag? tag)
          pages))

(defn by-year
  [pages]
  (->> pages
       (group-by post-year)
       (utils/reverse-sort-by first)))
