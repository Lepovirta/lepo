(ns lepo.resources
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [stasis.core :as stasis]))

(def config-filename "config.edn")
(def pages-dirname "pages")
(def page-pattern #"\.html$")

(defn load-config []
  (-> config-filename
      io/resource
      io/file
      slurp
      edn/read-string))

(defn raw-page-source []
  (stasis/slurp-resources pages-dirname page-pattern))

