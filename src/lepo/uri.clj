(ns lepo.uri
  (:require [lepo.utils :as utils]
            [clojure.string :as string]))

(defn remove-extension
  [^java.lang.String s]
  (let [index (.lastIndexOf s ".")]
    (if (> index 0)
      (.substring s 0 index)
      s)))

(defn absolute-path?
  [^String uri]
  (and (.startsWith uri "/")
       (not (.startsWith uri "//"))))

(defn add-root-path
  [root path]
  (if (and (not (empty? root))
           (absolute-path? path))
    (str "/" (utils/trim-slashes root) path)
    path))

(defn- slash-separated
  [& args]
  (->> args
       (map (comp utils/trim-slashes str))
       (remove string/blank?)
       (string/join "/")))

(defn parts->url
  [url-base & path-parts]
  (str url-base
       (apply slash-separated path-parts)))

(defn parts->path
  [& path-parts]
  (str "/" (apply slash-separated path-parts)))

(defn parts->dir
  [& path-parts]
  (str (apply parts->path path-parts) "/"))

(defn path->parts
  [path]
  (remove string/blank? (string/split path #"/")))