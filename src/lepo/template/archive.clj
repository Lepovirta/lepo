(ns lepo.template.archive
  (require [lepo.template.html :as html]))

(defn- html-group
  [{name :group posts :posts}]
  (list (html/heading 2 name)
        (html/post-list posts :archive-post)))

(defn template
  [{groups :groups}]
  (list [:header (html/heading 1 "Archive")]
        (mapcat html-group groups)))
