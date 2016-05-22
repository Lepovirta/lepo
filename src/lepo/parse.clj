(ns lepo.parse
  (:require [clojure.string :as string]
            [clojure.edn :as edn]
            [lepo.utils :as utils]
            [lepo.page :as page]))

(def page-separator #"\n\n---\n\n")

(defn- parse-file
  [filename content]
  (let [[header body] (string/split content page-separator 2)
        page-meta (edn/read-string header)
        page-type (page/path->page-type filename)]
    (assoc page-meta
           :content body
           :id (page/filename->page-id filename)
           :uri filename
           :page-type page-type)))

(defn parse-pages
  [conf pages-source]
  (map (partial apply parse-file) pages-source))
