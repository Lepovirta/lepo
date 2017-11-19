(ns lepo.build
  (:require [lepo.render :as render]
            [lepo.resources]
            [lepo.site.core]
            [lepo.parse :refer [parse-pages]]
            [stasis.core :as stasis]))

(defn- merge-sources
  [conf]
  (stasis/merge-page-sources
   (assoc (render/pages conf)
          :tags    (render/tags conf)
          :archive (render/archive conf)
          :rss     (render/rss conf))))

(defn- load-config
  [overrides]
  (merge (lepo.resources/load-config) overrides))

(defn build-site
  [overrides]
  (let [conf (load-config overrides)]
    (->> (lepo.resources/raw-page-source)
         parse-pages
         (lepo.site.core/build conf)
         merge-sources)))

(defn save!
  [site target-dir]
  (stasis/export-pages site target-dir))

(defn paths [site] (map first site))
