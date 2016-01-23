(ns lepo.export
  (:require [lepo.assets :as assets]
            [lepo.build :as build]
            [clojure.tools.logging :as log]
            [stasis.core :as stasis]))

(def default-export-target-dir
  (or (System/getenv "TARGET_DIR") "target/website/"))

(defn export
  ([target-dir]
   (log/info "exporting site to" target-dir)
   (log/info "emptying...")
   (stasis/empty-directory! target-dir)
   (log/info "saving assets...")
   (assets/save target-dir)
   (log/info "exporting pages...")
   (render/init-filters!)
   (stasis/export-pages (build/build-site) target-dir)
   (log/info "exporting site to" target-dir "done"))
  ([] (export default-export-target-dir)))

