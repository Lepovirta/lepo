(ns lepo.author
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lepo.uri]))

(def author-path "team")

(def authors-uri
  (lepo.uri/parts->path author-path "index.html"))

(defn uri
  [author-id]
  (lepo.uri/parts->dir author-path (name author-id)))

(defn full-uri
  [author-id]
  (lepo.uri/parts->path (uri author-id) "index.html"))

(defn expand-details
  [id author]
  (assoc author
         :uri      (uri id)
         :full-uri (full-uri id)))

(defn path->author-id
  [path]
  (let [[main-dir author-id] (lepo.uri/path->parts path)]
    (when (= main-dir author-path)
      (keyword author-id))))

(defn sorted-authors
  [authors]
  (->> authors (sort-by :name)))
