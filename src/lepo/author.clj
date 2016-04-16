(ns lepo.author
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lepo.utils :as utils]))

(def author-path "team")

(defn fullname
  [details]
  (let [firstname (:firstname details)
        lastname  (:lastname details)
        fullname  (str firstname " " lastname)]
    (string/trim fullname)))

(def authors-uri
  (utils/uri author-path "index.html"))

(defn uri
  [author-id]
  (utils/uri-dir author-path (name author-id)))

(defn full-uri
  [author-id]
  (utils/uri (uri author-id) "index.html"))

(defn expand-details
  [id details]
  (assoc details
         :fullname (fullname details)
         :uri      (uri id)
         :full-uri (full-uri id)))

(defn path->author-id
  [path]
  (let [[main-dir author-id] (utils/dir-list path)]
    (when (= main-dir author-path)
      (keyword author-id))))

(defn sorted-authors
  [conf]
  (->> (:authors conf)
       (map second)
       (sort-by :fullname)))
