(ns lepo.site.core
  (:require [lepo.uri :as uri]
            [lepo.author :as author]
            [lepo.page :as page]
            [lepo.site.conf]
            [lepo.site.author]
            [lepo.site.page]
            [lepo.site.post]
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
        (lepo.site.page/augment conf $)
        (update-in $ [:post]
                   (partial lepo.site.post/augment conf))
        (assoc $ :author
               (lepo.site.author/augment-author-pages conf $))))

(defn- get-posts
  [conf]
  (get-in conf [:pages :post]))

(defn- latest-posts
  [conf]
  (take (get conf :latest-posts-count 5)
        (get-posts conf)))

(defn- get-tags
  [conf]
  (-> conf get-posts page/pages->tags))

(defn- extend-conf
  [conf]
  (->> conf
       lepo.site.conf/augment
       lepo.site.root-path/add-root))

(defn- add-root-to-page
  [root page]
  (update-in page [:content]
             (partial lepo.site.root-path/add-root-to-html root)))

(defn- add-root-to-pages
  [{root :root-path} pages]
  (map (partial add-root-to-page root)
       pages))

(defn- pair->archive
  [[name posts]]
  {:group name :posts posts})

(defn post-archive
  [conf]
  (->> (get-posts conf)
       page/by-year
       (map pair->archive)))

(defn tag-uri
  [{root :root-path} tag]
  (uri/parts->path root (page/tag-uri tag)))

(defn posts-for-tag
  [conf tag]
  (page/filter-by-tag tag
                      (get-posts conf)))

(defn build
  [conf pages]
  (let [conf  (extend-conf conf)
        pages (add-root-to-pages conf pages)]
    (-> conf
        lepo.site.conf/augment
        (update-conf :authors lepo.site.author/augment-authors pages)
        (add-to-conf :pages process-pages pages)
        (add-to-conf :tags get-tags)
        (add-to-conf :latest-posts latest-posts)
        lepo.site.root-path/add-root)))
