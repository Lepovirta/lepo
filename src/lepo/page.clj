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

(defn author-fullname
  [post]
  (let [details (:author-details post)
        firstname (:firstname details)
        lastname (:lastname details)
        fullname (str firstname " " lastname)]
    (string/trim fullname)))

(defn- main-dir
  [path]
  (->> (string/split path #"/")
       (remove empty?)
       first))

(defn path->page-type
  [path]
  (get page-type-dirs (main-dir path) :default))

