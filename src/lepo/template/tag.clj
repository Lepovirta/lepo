(ns lepo.template.tag
  (require [lepo.template.html :as html]))

(defn template
  [{page :page}]
  (let [{posts :posts title :title} page]
    (list [:header (html/heading 1 title)]
          (html/post-list posts :default))))
