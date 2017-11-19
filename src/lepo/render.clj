(ns lepo.render
  (:require [lepo.template.core :as template]
            [lepo.rss :as rss]
            [lepo.page :as page]
            [lepo.site.core :as site]
            [lepo.utils :as utils]
            [lepo.author :as author]
            [hiccup.page]))

(defn- template-from-page
  [page]
  (or (:template page)
      (:page-type page)))

(defn- render
  [template-name conf]
  (hiccup.page/html5
   (lepo.template.core/template conf template-name)))

(defn page->html
  [conf page]
  (render (template-from-page page)
          (assoc conf :page page)))

(defn tag->html
  [conf tag]
  (render :tag
          (assoc conf
                 :name tag
                 :posts (site/posts-for-tag conf tag))))

(defn archives->html
  [conf]
  (render :archive
          (assoc conf :groups (site/post-archive conf))))

(defn- page-pair
  [conf page]
  [(:uri page) (fn [_] (page->html conf page))])

(defn- page-pairs
  [conf pages]
  (->> pages
       (map (partial page-pair conf))
       (into {})))

(defn pages
  [conf]
  (->> (:pages conf)
       (utils/map-values (partial page-pairs conf))
       (into {})))

(defn- tag-pair
  [conf tag]
  [(site/tag-uri conf tag) (fn [_] (tag->html conf tag))])

(defn tags
  [conf]
  (->> (:tags conf)
       (map (partial tag-pair conf))
       (into {})))

(defn archive
  [conf]
  {(:archive-uri conf) (fn [_] (archives->html conf))})

(defn rss
  [conf]
  {(:atom-uri conf) (fn [_] (rss/atom-xml conf))})
