(ns lepo.export
  (:require [lepo.assets :as assets]
            [lepo.build :as build]
            [lepo.manifest :as manifest]
            [clojure.tools.logging :as log]))

(def default-export-target-dir
  (or (System/getenv "TARGET_DIR") "target/website/"))

(defn- manifest-files
  [site assets]
  (concat (build/paths site)
          (assets/paths assets)))

(defn export
  ([target-dir root-dir]
   (log/info "exporting site to" target-dir)

   (let [orides {:root-path root-dir}
         site   (build/build-site orides)
         assets (assets/optimized-bundle orides)
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
