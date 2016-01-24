(ns lepo.build
  (:require [lepo.render :as render]
            [lepo.filters :as filters]
            [lepo.site :as site]
            [lepo.resources :as resources]
            [lepo.parse :refer [parse-pages]]
            [stasis.core :as stasis]))

(defn- merge-sources
  [conf]
  (stasis/merge-page-sources 
   (assoc (render/pages conf)
          :tags    (render/tags conf)
          :archive (render/archive conf)
          :authors (render/authors conf)
          :rss     (render/rss conf))))

(defn build-site []
  (let [conf (resources/load-config)]
    (->> (resources/raw-page-source)
         (parse-pages conf)
         (site/build conf)
         merge-sources)))

(defn save!
  [site target-dir]
  (filters/init!)
  (stasis/export-pages site target-dir))

(defn paths [site] (map first site))

