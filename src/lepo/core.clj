(ns lepo.core
  (:require [lepo.parse :refer [parse-pages]]
            [lepo.render :as render]
            [lepo.assets :as assets]
            [lepo.rss :as rss]
            [lepo.page :as page]
            [lepo.site :as site]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [stasis.core :as stasis]))

(def default-export-target-dir
  (or (System/getenv "TARGET_DIR") "target/website/"))

(defn- load-config []
  (-> "config.edn"
      io/resource
      io/file
      slurp
      edn/read-string))

(defn- raw-page-source []
  (stasis/slurp-resources "pages" #"\.html$"))

(defn- render-page
  [conf page]
  [(:uri page) (fn [_] (render/page conf page))])

(defn- render-pages
  [conf pages]
  (->> pages
       (map (partial render-page conf))
       (into {})))

(defn- all-pages
  [conf]
  (->> (:pages conf)
       (map (fn [[k v]] [k (render-pages conf v)]))
       (into {})))

(defn- tag-pair
  [conf tag]
  [(page/tag-uri tag) (fn [_] (render/tag conf tag))])

(defn- tags
  [conf]
  (->> (:tags conf)
       (map (partial tag-pair conf))
       (into {})))

(defn- archive
  [conf]
  {page/archive-uri (fn [_] (render/archives conf))})

(defn- rss
  [conf]
  {(:atom-uri conf) (fn [_] (rss/atom-xml conf))})

(defn- merge-sources
  [conf]
  (stasis/merge-page-sources 
    (assoc (all-pages conf)
           :tags (tags conf)
           :archive (archive conf)
           :rss (rss conf))))

(defn build-site []
  (let [conf (load-config)]
    (->> (raw-page-source)
         (parse-pages conf)
         (site/build conf)
         merge-sources)))

(defn app-init []
  (render/init-filters!)
  (selmer.parser/cache-off!))

(def app
  (-> (stasis/serve-pages build-site)
      assets/server 
      wrap-content-type))

(defn export
  ([target-dir]
   (log/info "exporting site to" target-dir)
   (log/info "emptying...")
   (stasis/empty-directory! target-dir)
   (log/info "saving assets...")
   (assets/save target-dir)
   (log/info "exporting pages...")
   (render/init-filters!)
   (stasis/export-pages (build-site) target-dir)
   (log/info "exporting site to" target-dir "done"))
  ([] (export default-export-target-dir)))

