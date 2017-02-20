(ns lepo.filters
  (:require [lepo.page :as page]
            [lepo.uri :as uri]
            [lepo.site :as site]
            [hiccup.core]
            [selmer.filters :refer [add-filter!]]
            [selmer.parser :refer [add-tag!]]))

(defn- safe-content
  [content]
  [:safe content])

(defn- hiccup
  [form]
  (hiccup.core/html form))

(def filters
  {:hiccup (comp safe-content hiccup)})

(defn- absolute
  [args context]
  (uri/add-root-path (:root-path context)
                     (first args)))

(defn- tag-uri
  [args context]
  (let [key (keyword (first args))
        tag (key context)]
    (site/tag-uri context tag)))

(defn- author-uri
  [args context]
  (let [key (keyword (first args))
        author (key context)]
    (or (:full-uri author)
        (:homepage author))))

(def tags
  {:absolute absolute
   :tag-uri tag-uri
   :author-uri author-uri})

(defn init! []
  (doseq [[k f] filters]
    (add-filter! k f))
  (doseq [[k t] tags]
    (add-tag! k t)))
