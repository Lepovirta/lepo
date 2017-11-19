(ns lepo.site.page
  (:require [lepo.utils :as utils]
            [lepo.uri :as uri]
            [lepo.site.conf]
            [lepo.site.root-path]
            [clojure.string :as string]))

(def page-type-dirs
  {lepo.site.conf/posts-path :post
   lepo.site.conf/author-path :author})

(def og-types
  {:post "article"})

(defn- path->page-type
  [path]
  (get page-type-dirs (first (uri/path->parts path)) :normal))

(defn- page-type->og-type
  [page-type]
  (get og-types page-type "website"))

(defn- open-graph
  [url page]
  {:type (page-type->og-type (:page-type page))
   :description (:description page)
   :url url
   :title (:title page)})

(defn- page-url
  [conf page]
  (uri/parts->url (:site-url conf) (:uri page)))

(defn- path->page-id
  [path]
  (-> path
      utils/trim-slashes
      uri/remove-extension
      (string/replace "/" ":")))

(defn- add-root-to-page
  [{root :root-path} page]
  (update-in page [:content]
             (partial lepo.site.root-path/add-root-to-html root)))

(defn- augment-page
  [conf page]
  (let [page (add-root-to-page conf page)
        url (page-url conf page)
        path (:path page)
        root-path (:root-path conf)
        page-type (path->page-type path)]
    (assoc page
           ;; date based on the filename
           :date (utils/str->date path)
           ;; normal page / post / etc.
           :page-type page-type
           ;; template fallback to page-type
           :template (or (:template page) page-type)
           ;; URN based on the path
           :id (path->page-id path)
           ;; full URL
           :url url
           ;; open graph data related to the page
           :og (open-graph url page))))

(defn- group-by-type
  [pages]
  (group-by :page-type pages))

(defn augment
  [conf pages]
  (->> pages
       (map (partial augment-page conf))
       group-by-type))
