(ns lepo.page
  (:require [lepo.utils :as utils]
            [clj-time.core :as t]
            [clojure.string :as string]))

(def posts-path "posts")
(def tags-path "tags")
(def author-path "team")

(def page-type-dirs
  {posts-path :post
   author-path :author})

(defn tag-uri
  [tag]
  (utils/uri tags-path (str tag ".html")))

(def archive-uri
  (utils/uri posts-path "index.html"))

(defn author-uri
  [post]
  (utils/uri-dir author-path
                 (-> post :author name)))

(defn has-tag?
  [tag post]
  (some #{tag} (:tags post)))

(defn post?
  [page]
  (= (:page-type page) :post))

(defn post-year [post] (t/year (:date post)))

(defn filename->page-id
  [filename]
  (-> filename utils/trim-slashes utils/remove-file-extension))

(defn author-fullname
  [details]
  (let [firstname (:firstname details)
        lastname (:lastname details)
        fullname (str firstname " " lastname)]
    (string/trim fullname)))

(defn post-author-fullname
  [post]
  (author-fullname (:author-details post)))

(defn- dir-list
  [path]
  (remove string/blank? (string/split path #"/")))

(defn path->author-id
  [path]
  (let [[main-dir author-id] (dir-list path)]
    (when (= main-dir author-path)
      (keyword author-id))))

(defn path->page-type
  [path]
  (get page-type-dirs (first (dir-list path)) :normal))

