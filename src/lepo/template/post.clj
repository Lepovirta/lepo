(ns lepo.template.post
  (require [lepo.template.html :as html]
           [lepo.template.utils :as utils]))

(defn- author-name-html
  [{name :name uri :uri}]
  (if uri
    (html/anchor uri name)
    name))

(defn- tag-list-item
  [root {tag-uri :uri tag :tag}]
  [:li (html/anchor tag-uri tag)])

(defn- tag-list
  [tags root]
  [:ul.inline-list
   (map (partial tag-list-item root) tags)])

(defn- prev-next-text
  [title date]
  (str title " " (utils/format-date date :navi)))

(defn- prev-link
  [{title :title date :date uri :uri}]
  (html/anchor uri
               (str "&laquo; " (prev-next-text title date))))

(defn- next-link
  [{title :title date :date uri :uri}]
  (html/anchor uri
               (str (prev-next-text title date) " &raquo")))

(defn- prev-next
  [{prev :prev
    next :next}]
  [:div {:id "prev-next"}
   (if prev (prev-link prev))
   (if next (next-link next))])

(defn- post-meta
  [{author :author
    date :date
    edited :edited
    tags :tags}
   root]
  [:div.post-meta
   [:div [:b "Author:"] " " (author-name-html author)]
   [:div [:b "Posted:"] " " (utils/format-date date)]
   (if edited
     [:div [:b "Edited:"] " " (utils/format-date edited)])
   (if tags
     [:div [:b "Tags:"] (tag-list tags root)])])

(defn template
  [conf]
  (let [page (:page conf)
        {content :content title :title} page
        root (:root-path conf)]
    (-> [[:header {} (html/heading 1 title)]]
        (into content)
        (conj (post-meta page root)
              (prev-next page)))))
