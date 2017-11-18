(ns lepo.template.normal
  (require [lepo.template.html :as html]))

(defn template
  [conf]
  (let [page (:page conf)
        {title :title content :content} page]
    (list [:header {} (html/heading 1 title)]
          content)))
