(ns lepo.template.utils
  (:require [clj-time.format :as time-format]
           [lepo.utils]))

(def time-formats-str
  {:archive-post "MMM dd"
   :navi         "yy/MM"
   :default      "yyy-MM-dd"})

(def time-formats
  (into {}
        (lepo.utils/map-values time-format/formatter
                               time-formats-str)))

(defn format-date
  ([date format]
   (time-format/unparse (get time-formats format) date))
  ([date]
   (format-date date :default)))
