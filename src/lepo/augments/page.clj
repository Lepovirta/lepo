(ns lepo.augments.page
  (:require [lepo.page :as page]))

(defn- open-graph
  [url page]
  {:type (page/page-type->og-type (:page-type page))
   :description (:description page)
   :url url
   :title (:title page)})

(defn- page-url
  [conf page]
  (page/page-url (:site-url conf) page))

(defn- augment-page
  [conf page]
  (let [url (page-url conf page)]
    (assoc page
           :url url
           :og (open-graph url page))))

(defn augment
  [conf pages]
  (->> pages
       (map (partial augment-page conf))
       page/group-by-type))
