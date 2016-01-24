(ns lepo.assets
  (:require [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]
            [optimus-sass.core]))

(def asset-dir "assets")

(defn- sass-bundle []
  (assets/load-bundle
   asset-dir
   "main.css"
   [#"/styles/.+\.scss"]))

(def static-asset-files
  ["/favicon.png"
   #"/img/.*"
   #"/files/.*"])

(defn- static-file-bundle []
  (assets/load-assets asset-dir static-asset-files))

(defn normal-bundle []
  (concat
   (sass-bundle)
   (static-file-bundle)))

(defn optimized-bundle []
  (-> (normal-bundle)
      (optimizations/minify-css-assets {})))

(defn server
  [app]
  (optimus/wrap app normal-bundle optimizations/none serve-live-assets))

(defn save!
  [bundle target-dir]
  (optimus.export/save-assets bundle target-dir))

(defn paths [bundle] (map :path bundle))

