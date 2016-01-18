(ns lepo.render
  (:require [selmer.parser :as selmer]
            [selmer.filters :refer [add-filter!]]
            [lepo.page :as page]
            [lepo.site :as site]
            [lepo.utils :as utils]
            [clj-time.core :as t]
            [clojure.java.io :as io]))

(defn init-filters! []
  (add-filter! :tag-uri page/tag-uri)
  (add-filter! :author-fullname page/post-author-fullname)
  (add-filter! :author-uri page/author-uri))

(defn- template-path
  [template-name]
  (.getPath (io/file "templates" (str template-name ".html"))))

(defn- template-from-page
  [page]
  (or (:template page) (-> page :page-type name)))

(defn- render
  [template-name conf]
  (selmer/render-file (template-path template-name) conf))

(defn page
  [conf page]
  (render (template-from-page page)
          (assoc conf :page page)))

(defn tag
  [conf tag]
  (render "tag"
          (assoc conf
                 :name tag
                 :posts (site/posts-for-tag conf tag))))

(defn archives
  [conf]
  (render "archives"
          (assoc conf :groups (site/posts-by-year conf))))

