(ns lepo.template.archive
  (:require [lepo.template.html :as html]))

(defn- html-archive-group
  [{name :name posts :posts}]
  (list (html/heading 2 name)
        (html/post-list posts :archive-post)))

(defn template
  [{archive :archive}]
  (list [:header (html/heading 1 "Archive")]
        (mapcat html-archive-group archive)))
