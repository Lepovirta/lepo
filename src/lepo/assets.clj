(ns lepo.assets
  (:require [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]
            [optimus-sass.core]
            [lepo.uri :as uri]))

(def asset-dir "assets")

(defn- sass-bundle []
  (assets/load-bundle
   asset-dir
   "main.css"
   [#"/styles/.+\.scss"]))

(def static-asset-files
  ["/favicon.png"
   #"/img/.*"
   ;#"/styles/.+\.css"
   ;#"/files/.*"
])

(defn- static-file-bundle []
  (assets/load-assets asset-dir static-asset-files))

(defn- assets-with-root
  [bundle root]
  (map (fn [assets]
         (update-in assets [:path]
                    (partial uri/add-root-path root)))
       bundle))

(defn normal-bundle []
  (concat
   (sass-bundle)
   (static-file-bundle)))

(defn optimized-bundle [{root :root-path}]
  (-> (normal-bundle)
      (optimizations/minify-css-assets {})
      (assets-with-root root)))

(defn server
  [app]
  (optimus/wrap app normal-bundle optimizations/none serve-live-assets))

(defn save!
  [bundle target-dir]
  (optimus.export/save-assets bundle target-dir))

(defn paths [bundle] (map :path bundle))
