(ns lepo.template.html
  (:require [lepo.template.utils :as utils]))

(defn heading
  [level text]
  [(keyword (str "h" level)) {} text])

(defn anchor
  [uri text & attributes]
  [:a (assoc (apply hash-map attributes) :href uri) text])

(defn- post-list-item
  [time-format {date :date uri :uri title :title}]
  [:li
   (utils/format-date date time-format)
   " &raquo; "
   [:a {:href uri} title]])

(defn post-list
  [items time-format]
  [:ul.no-bul
   (map (partial post-list-item time-format)
        items)])
