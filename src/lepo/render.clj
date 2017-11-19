(ns lepo.render
  (:require [lepo.template.core :as template]
            [lepo.site.core :as site]
            [lepo.utils :as utils]
            [hiccup.page]
            [clojure.data.xml :as xml]))

(defn- render
  [template-name conf]
  (hiccup.page/html5
   (lepo.template.core/template conf template-name)))

(defn page->html
  [conf page]
  (render (:template page)
          (assoc conf :page page)))

(defn archives->html
  [conf]
  (render :archive conf))

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

(defn archive
  [conf]
  {(:archive-uri conf) (fn [_] (archives->html conf))})

(defn- feed->xml
  [feed]
  (->> feed xml/sexp-as-element xml/emit-str))

(defn atom-feed
  [conf]
  {(:atom-uri conf) (fn [_] (-> conf template/atom-feed feed->xml))})
