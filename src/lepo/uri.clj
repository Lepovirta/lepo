(ns lepo.uri
  (:require [lepo.utils :as utils]
            [clojure.string :as string]))

(defn remove-extension
  [^String s]
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
  (let [root (utils/trim-slashes root)]
    (if (and (not (empty? root))
             (absolute-path? path))
      (str "/" root path)
      path)))

(defn- slash-separated
  [parts]
  (->> parts
       (map (comp utils/trim-slashes str))
       (remove string/blank?)
       (string/join "/")))

(defn parts->path
  [& path-parts]
  (let [path-parts (remove string/blank? path-parts)
        last-part (last path-parts)
        is-dir? (and (string? last-part) (.endsWith last-part "/"))
        path-start "/"
        path-end (if is-dir? "/" "")
        joined-parts (slash-separated path-parts)]
    (if (string/blank? joined-parts)
      path-start
      (str path-start joined-parts path-end))))

(defn parts->url
  [url-base & path-parts]
  (str (utils/trim-slashes url-base)
       (apply parts->path path-parts)))

(defn parts->dir
  [& path-parts]
  (apply parts->path
         (concat path-parts (list "/"))))

(defn path->parts
  [path]
  (remove string/blank? (string/split path #"/")))
