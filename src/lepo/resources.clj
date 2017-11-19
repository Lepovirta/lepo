(ns lepo.resources
  (:require [clojure.java.io :as io]
            [stasis.core :as stasis]))

(def pages-dirname "pages")
(def page-pattern #"\.html$")

(defn raw-page-source []
  (stasis/slurp-resources pages-dirname page-pattern))
