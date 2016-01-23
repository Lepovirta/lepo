(ns lepo.live
  (:require [stasis.core :as stasis]
            [lepo.render :as render]
            [lepo.build :as build]
            [lepo.assets :as assets]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn app-init []
  (render/init-filters!)
  (selmer.parser/cache-off!))

(def app
  (-> (stasis/serve-pages build/build-site)
      assets/server 
      wrap-content-type))
