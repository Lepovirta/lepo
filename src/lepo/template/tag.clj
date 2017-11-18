(ns lepo.template.tag
  (require [lepo.template.html :as html]))

(defn page-title
  [tag]
  (str "Posts Tagged &quot;" tag "&quot;"))

(defn template
  [{posts :posts name :name}]
  (list [:header (html/heading 1 (page-title name))]
        (html/post-list posts :default)))
