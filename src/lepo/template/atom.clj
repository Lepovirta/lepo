(ns lepo.template.atom
  (:require [hiccup.core :as hiccup]
            [lepo.uri]))

(defn- post
  [conf post]
  [:entry
   [:title (:title post)]
   [:updated (:date post)]
   [:author [:name (-> post :author :name)]]
   [:link {:href (:url post)}]
   [:id (str (:site-urn conf) ":post:" (:id post))]
   [:content
    {:type "html"}
    (-> post :content hiccup/html)]])

(defn from-conf
  [conf]
  (let [posts    (get-in conf [:pages :post])
        atom-url (lepo.uri/parts->url (:site-url conf) (:atom-uri conf))]
    [:feed {:xmlns "http://www.w3.org/2005/Atom"}
     [:id (:site-urn conf)]
     [:updated (-> posts first :date)]
     [:title {:type "text"} (:site-title conf)]
     [:link {:rel "self" :href atom-url}]
     (map (partial post conf) posts)]))
