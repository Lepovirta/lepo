(ns lepo.live
  (:require [stasis.core :as stasis]
            [lepo.render :as render]
            [lepo.filters :as filters]
            [lepo.build :as build]
            [lepo.assets :as assets]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn app-init []
  (filters/init!)
  (selmer.parser/cache-off!))

(def app
  (-> (stasis/serve-pages (partial build/build-site {}))
      assets/server
      wrap-content-type))
