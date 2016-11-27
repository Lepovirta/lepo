(ns lepo.export
  (:require [lepo.assets :as assets]
            [lepo.build :as build]
            [lepo.uri :as uri]
            [lepo.html :as html]
            [lepo.manifest :as manifest]
            [clojure.tools.logging :as log]))

(def default-export-target-dir
  (or (System/getenv "TARGET_DIR") "target/website/"))

(defn- manifest-files
  [site assets]
  (concat (build/paths site)
          (assets/paths assets)))

(defn- page-with-root
  [root path page]
  [(uri/add-root-path root path)
   (fn [context]
     (html/with-html (page context)
       (partial html/add-root-path root)))])

(defn- site-with-root
  [root site]
  (into {}
        (map (fn [[path page]]
               (page-with-root root path page))
             site)))

(defn- assets-with-root
  [root bundle]
  (map (fn [assets]
         (update-in assets [:path] (partial uri/add-root-path root)))
       bundle))

(defn export
  ([target-dir root-dir]
   (log/info "exporting site to" target-dir)

   (let [site   (site-with-root root-dir (build/build-site))
         assets (assets-with-root root-dir (assets/optimized-bundle))
         paths  (manifest-files site assets)]

     (log/info "emptying target directory")
     (manifest/delete-manifest-files! target-dir)

     (log/info "writing manifest")
     (manifest/write! paths target-dir)

     (log/info "saving assets")
     (assets/save! assets target-dir)

     (log/info "exporting pages")
     (build/save! site target-dir)

     (log/info "exporting site to" target-dir "done")))

  ([target-dir] (export target-dir ""))

  ([] (export default-export-target-dir "")))
