(ns lepo.site.conf
  (:require [lepo.uri :as uri]
            [lepo.utils :as utils]
            [lepo.page]
            [lepo.author]
            [lepo.rss]))

(defn augment
  [conf]
  (let [conf (update-in conf [:root-path] uri/parts->dir)
        root (:root-path conf)
        conf (update-in conf [:site-url] uri/parts->url root)]
    (assoc conf
           :servlet-context (utils/remove-trailing-slashes root)
           :archive-uri lepo.page/archive-uri
           :authors-uri lepo.author/authors-uri
           :atom-uri lepo.rss/uri)))
