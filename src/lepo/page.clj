(ns lepo.page
  (:require [lepo.utils :as utils]
            [clojure.string :as string]))

(def posts-path "posts")
(def tags-path "tags")

(defn tag-uri
  [tag]
  (utils/uri tags-path (str tag ".html")))

(def archive-uri
  (utils/uri posts-path "index.html"))

(defn has-tag?
  [tag post]
  (some #{tag} (:tags post)))

(defn author-fullname
  [post]
  (let [details (:author-details post)
        firstname (:firstname details)
        lastname (:lastname details)
        fullname (str firstname " " lastname)]
    (string/trim fullname)))

