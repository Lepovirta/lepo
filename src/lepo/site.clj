(ns lepo.site
  (:require [lepo.uri :as uri]
            [lepo.author :as author]
            [lepo.page :as page]
            [lepo.augments.conf]
            [lepo.augments.author]
            [lepo.augments.page]
            [lepo.augments.post]
            [lepo.augments.projects]
            [lepo.augments.root-path]))

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
    (lepo.augments.page/augment conf $)
    (update-in $ [:post]
               (partial lepo.augments.post/augment conf))
    (assoc $ :author
           (lepo.augments.author/augment-author-pages conf $))))

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
       lepo.augments.conf/augment
       lepo.augments.root-path/add-root))

(defn- add-root-to-page
  [root page]
  (update-in page [:content]
             (partial lepo.augments.root-path/add-root-to-html root)))

(defn- add-root-to-pages
  [{root :root-path} pages]
  (map (partial add-root-to-page root)
       pages))

(defn- pair->group
  [[group posts]]
  {:group group :posts posts})

(defn post-archive
  [conf]
  (->> (get-posts conf)
       page/by-year
       (map pair->group)))

(defn tag-uri
  [{root :root-path} tag]
  (uri/parts->path root (page/tag-uri tag)))

(defn posts-for-tag
  [conf tag]
  (page/filter-by-tag tag
                      (get-posts conf)))

(defn authors
  [conf]
  (->> (:authors conf)
       (map second)
       (author/sorted-authors)))

(defn build
  [conf pages]
  (let [conf  (extend-conf conf)
        pages (add-root-to-pages conf pages)]
    (-> conf
        lepo.augments.conf/augment
        (update-conf :authors lepo.augments.author/augment-authors pages)
        (add-to-conf :pages process-pages pages)
        (update-conf :projects lepo.augments.projects/augment)
        (add-to-conf :tags get-tags)
        (add-to-conf :latest-posts latest-posts)
        lepo.augments.root-path/add-root)))
