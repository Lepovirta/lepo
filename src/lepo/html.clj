(ns lepo.html
  (:require [pl.danieljanus.tagsoup :as tagsoup]
            [hiccup.core :as hiccup]
            [lepo.uri :as uri]))

(defn with-html
  [html-string f]
  (-> html-string
      tagsoup/parse-string
      f
      hiccup/html))

(def attr-names-with-uris
  #{:src :href})

(defn- add-root-to-attr
  [root name value]
  (if (attr-names-with-uris name)
    (uri/add-root-path root value)
    value))

(defn- add-root-to-attrs
  [root attrs]
  (into {}
        (map (fn [[name value]]
               [name (add-root-to-attr root name value)])
             attrs)))

(defn add-root-path
  [root html]
  (if (and (not (empty? root))
           (vector? html))
    (let [[tag attrs & body] html
          updated-attrs (add-root-to-attrs root attrs)
          updated-body (map (partial add-root-path root) body)]
      (vec (concat [tag updated-attrs] updated-body)))
    html))
