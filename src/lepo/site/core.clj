(ns lepo.site.core
  (:require [lepo.uri :as uri]
            [lepo.utils :as utils]
            [lepo.site.archive]
            [lepo.site.conf]
            [lepo.site.author]
            [lepo.site.page]
            [lepo.site.post]
            [lepo.site.tag]
            [lepo.site.root-path]))

(defn- update-conf
  [conf k f & args]
  (update-in conf [k]
             (fn [v]
               (apply f (conj args v conf)))))

(defn- add-to-conf
  [conf k f & args]
  (assoc conf k
         (apply f (cons conf args))))

(defn- process-pages
  [conf pages]
  (as-> pages $
    ;; augment all pages
    (lepo.site.page/augment conf $)
    ;; augment posts
    (update-in $ [:post]
               (partial lepo.site.post/augment conf))
    ;; augment author pages
    (assoc $ :author
           (lepo.site.author/augment-author-pages conf $))
    ;; add tag pages
    (assoc $ :tag (lepo.site.tag/tag-pages conf $))))

(defn- pages->paths
  [pages]
  (into #{} (map :path pages)))

(defn build
  [overrides pages]
  (let [conf  (lepo.site.conf/build overrides)
        paths (pages->paths pages)]
    (-> conf
        (update-conf :authors lepo.site.author/augment-authors paths)
        (add-to-conf :tags lepo.site.tag/all-tags pages)
        (add-to-conf :pages process-pages pages)
        (add-to-conf :archive lepo.site.archive/conf->archive)
        (add-to-conf :latest-posts lepo.site.archive/latest-posts)
        lepo.site.root-path/add-root)))
