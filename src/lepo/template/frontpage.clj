(ns lepo.template.frontpage
  (require [lepo.template.html :as html]))

(defn- more-posts-ad
  [{archive-uri :archive-uri
    atom-uri :atom-uri}]
  [:p
   "Check out the "
   (html/anchor archive-uri "Archive")
   " for more posts. Subscribe to "
   (html/anchor atom-uri "RSS feed")
   " for the latest posts."])

(defn- author-uri
  [author]
  (or (:full-uri author)
      (:homepage author)))

(defn- author-list-item
  [[author-id author]]
  [:li
   (html/anchor (author-uri author)
                (:name author))])

(defn- authors-list
  [authors]
  [:ul (map author-list-item authors)])

(defn- tail-html
  [{authors :authors
    posts :latest-posts
    :as conf}]
  [(html/heading 2 "Meet the team")
   "We are the Lepo members."
   (authors-list authors)
   (html/heading 2 "Recent posts")
   (html/post-list posts :default)
   (more-posts-ad conf)])

(defn template
  [{page :page :as conf}]
  (concat (:content page)
          (tail-html conf)))
