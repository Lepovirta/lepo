(ns lepo.filters
  (:require [lepo.page :as page]
            [selmer.filters :refer [add-filter!]]))

(def filters
  {:tag-uri page/tag-uri})

(defn init! []
  (doseq [[k f] filters]
    (add-filter! k f)))

