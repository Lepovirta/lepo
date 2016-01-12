(ns lepo.core
  (:require [lepo.parse :as parse]
            [lepo.render :as render]
            [lepo.assets :as assets]
            [lepo.rss :as rss]
            [lepo.page :as page]
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

(defn- render-kv
  [renderer site m]
  [(:uri m) (fn [_] (renderer site m))])

(defn- render-key
  [site renderer k]
  (->> (get site k)
       (map (partial render-kv renderer site))
       (into {})))

(defn- pages
  [site k]
  (render-key site render/page k))

(defn- tag-pair
  [site tag]
  [(page/tag-uri tag) (fn [_] (render/tag site tag))])

(defn- tags
  [site]
  (->> (:tags site)
       (map (partial tag-pair site))
       (into {})))

(defn- archive
  [site]
  {page/archive-uri (fn [_] (render/archives site))})

(defn- rss
  [site]
  {(:atom-uri site) (fn [_] (rss/atom-xml site))})

(defn- parse-site []
  (parse/site (load-config) (raw-page-source)))

(defn site []
  (let [s (parse-site)]
    (stasis/merge-page-sources
      {:pages (pages s :pages)
       :posts (pages s :posts)
       :tags (tags s)
       :archive (archive s)
       :rss (rss s)})))

(defn app-init []
  (render/init-filters!)
  (selmer.parser/cache-off!))

(def app
  (-> (stasis/serve-pages site)
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
   (stasis/export-pages (site) target-dir)
   (log/info "exporting site to" target-dir "done"))
  ([] (export default-export-target-dir)))

