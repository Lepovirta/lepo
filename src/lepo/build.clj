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
          :archive (render/archive conf)
          :atom    (render/atom-feed conf))))

(defn build-site
  [overrides]
  (->> (lepo.resources/raw-page-source)
       parse-pages
       (lepo.site.core/build overrides)
       merge-sources))

(defn save!
  [site target-dir]
  (stasis/export-pages site target-dir))

(defn paths [site] (map first site))
