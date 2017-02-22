(ns lepo.parse
  (:require [clojure.string :as string]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [hickory.core :as hickory]
            [lepo.utils :as utils]
            [lepo.page :as page]))

(def page-separator #"\n\n---\n\n")

(def parse-meta edn/read-string)

(def parse-content
  (comp (partial map hickory/as-hiccup)
        hickory/parse-fragment))

(defn- split-page
  [content]
  (string/split content page-separator 2))

(defn- build-page
  [meta body filename]
  (assoc meta
         ;; HTML contents as a sequence of sexps
         :content body
         ;; original path
         :path filename
         ;; final path to the file
         :uri filename
         ;; blog post / profile page / normal page
         :page-type (page/path->page-type filename)
         ;; date based on the filename
         :date (utils/str->date filename)
         ;; URN based on the filename
         :id (page/filename->page-id filename)))

(defn- parse-file
  [filename content]
  (let [[header-str body-str] (split-page content)
        page (build-page (parse-meta header-str)
                         (parse-content body-str)
                         filename)]
    (log/debug "Parsed a" (:page-type page) "page"
               "with ID" (:id page)
               "from" filename)
    page))

(defn parse-pages
  [pages-source]
  (map (partial apply parse-file)
       pages-source))
