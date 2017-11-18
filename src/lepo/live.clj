(ns lepo.live
  (:require [clojure.tools.logging :as log]
            [lepo.build]
            [lepo.assets]
            [stasis.core :as stasis]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn app-init []
  (log/info "Initialising"))

(def app
  (-> (stasis/serve-pages (partial lepo.build/build-site {}))
      lepo.assets/server
      wrap-content-type))
