(ns lepo.live
  (:require [lepo.selmer]
            [lepo.build]
            [lepo.assets]
            [stasis.core :as stasis]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn app-init []
  (lepo.selmer/init!)
  (selmer.parser/cache-off!))

(def app
  (-> (stasis/serve-pages (partial lepo.build/build-site {}))
      lepo.assets/server
      wrap-content-type))
