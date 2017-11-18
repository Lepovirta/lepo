(ns lepo.template.author
  (require [lepo.template.html :as html]))

(defn- html-posts
  [author-posts]
  (list (html/heading 2 "Posts")
        (html/post-list author-posts :default)))

(defn template
  [conf]
  (let [page (:page conf)
        {title :title
         content :content
         author-posts :author-posts} page]
    (list [:header [:h1 title]]
          content
          (if author-posts (html-posts author-posts)))))
