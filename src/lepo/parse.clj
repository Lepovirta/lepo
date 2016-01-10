(ns lepo.parse
  (:require [clojure.string :as string]
            [clojure.edn :as edn]
            [lepo.utils :as utils]
            [lepo.page :as page]
            [clj-time.core :as t]
            [clj-time.format :as tf]))

(def page-separator #"\n\n---\n\n")

(def date-formatter (tf/formatter "yyyy-MM-dd"))

(def date-pattern #"\d+-\d+-\d+")

(defn- parse-date
  [s]
  (tf/parse date-formatter (re-find date-pattern s)))

(defn- posts->tags
  [posts]
  (->> posts (mapcat :tags) distinct sort))

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

(defn- latest-posts
  [conf posts]
  (take (get conf :latest-posts 5) posts))

(defn- parse-content
  [content]
  (let [parts (string/split content page-separator 2)
        page-meta (edn/read-string (first parts))
        content (second parts)]
    (assoc page-meta :content content)))

(defn- filename->page-id
  [filename]
  (-> filename utils/trim-slashes utils/remove-file-extension))

(defn- parse-page
  [conf filename content & {:keys [uri-prefix] :or {uri-prefix ""}}]
  (assoc (parse-content content)
         :id (filename->page-id filename)
         :uri (utils/uri uri-prefix filename)))

(defn- parse-post
  [conf filename content]
  (let [page (parse-page conf filename content :uri-prefix page/posts-path)
        author-id (:author page)
        author-details (get-in conf [:authors author-id])
        date (parse-date filename)]
    (assoc page
           :date date
           :author-details author-details)))

(defn- reverse-compare [a b] (compare b a))

(defn- parse-pages
  [conf coll]
  (->> coll
       (map (partial apply parse-page conf))
       (sort-by :title)))

(defn- parse-posts
  [conf coll]
  (->> coll
       (map (partial apply parse-post conf))
       (sort-by :date reverse-compare)
       link-posts))

(defn site
  [conf pages-source posts-source]
  (let [pages (parse-pages conf pages-source)
        posts (parse-posts conf posts-source)]
    (assoc conf
           :all-pages pages
           :all-posts posts
           :latest-posts (latest-posts conf posts)
           :tags (posts->tags posts))))

