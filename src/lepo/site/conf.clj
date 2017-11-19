(ns lepo.site.conf
  (:require [lepo.uri :as uri]))

(def authors-list
  [{:id :jkpl
    :name "Jaakko Pallari"
    :homepage "https://lepo.io/team/jkpl/"}
   {:id :esajuhana
    :name "Esa Juhana"
    :homepage "http://www.esajuhana.com/"}
   {:id :matson
    :name "Matias Keveri"
    :homepage "http://matias.keveri.fi/"}
   {:id :visav
    :name "Visa Varjus"
    :homepage "https://github.com/visav"}])

(def authors-map
  (->> authors-list
       (map (fn [author] [(:id author) author]))
       (into {})))

(def atom-uri "/atom.xml")
(def posts-path "posts")
(def archive-uri (uri/parts->path posts-path "index.html"))
(def author-path "team")
(def authors-uri (lepo.uri/parts->path author-path "index.html"))
(def tags-path "tags")
(def tags-uri (uri/parts->path tags-path "index.html"))

(def base-config
  {:site-title       "Lepo"
   :site-description "We are a group of developers who enjoy making open source software."
   :site-url         "https://lepo.io/"
   :site-urn         "urn:lepo:feed"
   :authors          authors-map
   :archive-uri      archive-uri
   :authors-uri      authors-uri
   :atom-uri         atom-uri})

(defn build
  [overrides]
  (let [conf (merge base-config overrides)
        conf (update-in conf [:root-path] uri/parts->dir)
        root (:root-path conf)]
    (update-in conf [:site-url]
               uri/parts->url root)))
