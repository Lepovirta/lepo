(ns lepo.template.core
  (:require [lepo.template.archive]
            [lepo.template.author]
            [lepo.template.base]
            [lepo.template.frontpage]
            [lepo.template.normal]
            [lepo.template.post]
            [lepo.template.tag]
            [lepo.template.atom]))

(def sub-templates
  {:archive   lepo.template.archive/template
   :author    lepo.template.author/template
   :frontpage lepo.template.frontpage/template
   :normal    lepo.template.normal/template
   :post      lepo.template.post/template
   :tag       lepo.template.tag/template})

(def default-sub-template
  lepo.template.normal/template)

(defn- get-sub-template
  [template-key]
  (if (nil? template-key)
    default-sub-template
    (sub-templates template-key)))

(defn template
  [conf template-key]
  (let [sub-template (get-sub-template template-key)
        content      (sub-template conf)]
    (lepo.template.base/template conf content)))

(def atom-feed lepo.template.atom/from-conf)
