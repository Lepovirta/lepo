(ns lepo.author
  (:require [clojure.string :as string]
            [lepo.utils :as utils]))

(def author-path "team")

(def link-expansions
  {:twitter (partial str "https://twitter.com/")
   :github  (partial str "https://github.com/")})

(defn fullname
  [details]
  (let [firstname (:firstname details)
        lastname  (:lastname details)
        fullname  (str firstname " " lastname)]
    (string/trim fullname)))

(defn uri
  [author-id]
  (utils/uri-dir author-path (name author-id)))

(defn full-uri
  [author-id]
  (utils/uri (uri author-id) "index.html"))

(defn- expand-link
  [id fragment]
  ((get link-expansions id identity) fragment))

(defn- expand-links
  [links]
  (map (fn [[id fragment]] (expand-link id fragment))
       links))

(defn expand-details
  [id details]
  (assoc details
         :fullname (fullname details)
         :uri      (uri id)
         :full-uri (full-uri id)
         :links    (expand-links (:links details))))

(defn path->author-id
  [path]
  (let [[main-dir author-id] (utils/dir-list path)]
    (when (= main-dir author-path)
      (keyword author-id))))

