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

(defn- parse-normal-page
  [conf filename content]
  (assoc (parse-content content)
         :page-type :normal
         :id (filename->page-id filename)
         :uri filename))

(defn- parse-post
  [conf filename content]
  (let [page (parse-normal-page conf filename content)
        author-id (:author page)
        author-details (get-in conf [:authors author-id])
        date (parse-date filename)]
    (assoc page
           :page-type :post
           :date date
           :author-details author-details)))

(def page-parsers
  {page/posts-path parse-post})

(defn- parser-for-file
  [filename]
  (or (get page-parsers (page/main-dir filename))
      parse-normal-page))

(defn- parse-page
  [conf filename content]
  ((parser-for-file filename) conf filename content))

(defn parse-pages
  [conf coll]
  (->> coll
       (map (partial apply parse-page conf))
       (sort-by :title)))

(defn- filter-pages
  [pages]
  (remove page/post? pages))

(defn- filter-posts
  [pages]
  (->> pages
       (filter page/post?)
       (utils/reverse-sort-by :date)
       link-posts))

(defn site
  [conf pages-source]
  (let [all-pages (parse-pages conf pages-source)
        pages (filter-pages all-pages)
        posts (filter-posts all-pages)]
    (assoc conf
           :pages pages
           :posts posts
           :latest-posts (latest-posts conf posts)
           :tags (posts->tags posts))))

