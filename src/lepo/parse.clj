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

(defn- str->date
  [s]
  (tf/parse date-formatter (re-find date-pattern s)))

(defn- filename->page-id
  [filename]
  (-> filename utils/trim-slashes utils/remove-file-extension))

(defn- parse-file
  [filename content]
  (let [[header body] (string/split content page-separator 2)
        page-meta (edn/read-string header)]
    (assoc page-meta
           :content body
           :id (filename->page-id filename)
           :uri filename)))

(defn- augment-post
  [conf filename page]
  (let [author-id (:author page)
        author-details (get-in conf [:authors author-id])
        date (str->date filename)]
    {:date date :author-details author-details}))

(defn- augment-author
  [conf filename page]
  {}) ; TODO

(def page-augmentations
  {:post augment-post
   :author augment-author})

(defn- no-augments [& args] {})

(defn- augmentation-for-file
  [filename]
  (let [page-type (page/path->page-type filename)
        augmenter (get page-augmentations page-type no-augments)]
    (fn [conf page]
      (assoc (augmenter conf filename page)
             :page-type page-type))))

(defn- augment-page
  [conf filename page]
  (let [augmenter (augmentation-for-file filename)
        augments (augmenter conf page)]
    (into page augments)))

(defn- parse-page
  [conf filename content]
  (->> content
       (parse-file filename)
       (augment-page conf filename)))

(defn parse-pages
  [conf pages-source]
  (map (partial apply parse-page conf) pages-source))

