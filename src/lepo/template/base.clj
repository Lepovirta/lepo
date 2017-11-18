(ns lepo.template.base
  (require [lepo.template.html :as html]
           [lepo.uri]))

(def viewport "width=device-width, initial-scale=1")
(def theme-color "#44118d")
(def og-image-path "img/lepologo.png")
(def license-url "http://creativecommons.org/licenses/by-sa/4.0/")
(def license-text "CC BY-SA 4.0")
(def favicon-path "/favicon.png")

(defn- page-title
  [title subtitle]
  (if subtitle
    (str title " - " subtitle)
    title))

(defn- og-tags
  [kvs]
  (for [[k v] kvs]
    [:meta {:property (str "og:" (name k))
            :content v}]))

(defn- html-head
  [{page :page
    site-title :site-title
    site-url :site-url
    atom-uri :atom-uri
    root-path :root-path
    :as conf}]
  [:head
   [:meta {:charset "utf-8"}]
   [:title (page-title site-title (:title page))]
   [:meta {:name "viewport" :content viewport}]
   [:meta {:name "theme-color" :content theme-color}]
   [:meta {:name "description" :content (:description page)}]
   [:meta {:property "og:image" :content (str site-url og-image-path)}]
   [:meta {:property "og:site_name" :content site-title}]
   [:link {:rel "icon" :type "image/png"
           :href (lepo.uri/add-root-path root-path favicon-path)}]
   [:link {:rel "alternate" :type "application/atom+xml"
           :title "posts" :href atom-uri}]
   [:link {:rel "stylesheet" :type "text/css"
           :href (lepo.uri/add-root-path root-path "/styles/main.css")}]
   (og-tags (:og page))])

(defn- html-body
  [{page :page
    root-path :root-path
    site-title :site-title
    :as conf}
   content]
  [:body
   [:div#top (html/anchor root-path site-title)]
   [:div.wrapper
    (into [:main {:role "main"}] content)
    [:footer
     [:div.left
      (html/anchor license-url license-text :rel "license")]
     [:div.right
      (html/anchor "#top" "TOP")]]]])

(defn template
  [conf content]
  [:html {:lang "en"}
   (html-head conf)
   (html-body conf content)])
