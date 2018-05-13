(ns lepo.template.utils
  (:require [java-time]
            [lepo.utils]))

(def time-formats-str
  {:archive-post "MMM dd"
   :navi         "yy/MM"
   :default      "yyy-MM-dd"})

(def time-formats
  (into {}
        (lepo.utils/map-values java-time/formatter
                               time-formats-str)))

(defn format-date
  ([date format]
   (java-time/format (get time-formats format) date))
  ([date]
   (format-date date :default)))
