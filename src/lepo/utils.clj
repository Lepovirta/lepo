(ns lepo.utils
  (:require [clojure.string :as string]))

(def slash-pattern #"^[/]*")
(def trailing-slashes-pattern #"[/]*$")
(def leading-slashes-pattern #"^[/]*")

(defn trim-slashes
  [s]
  (-> s
      (string/replace-first leading-slashes-pattern "")
      (string/replace-first trailing-slashes-pattern "")))

(defn remove-file-extension
  [s]
  (let [index (.lastIndexOf s ".")]
    (if (> index 0)
      (.substring s 0 index)
      s)))

(defn uri
  [& args]
  (->> args
       (map str)
       (map trim-slashes)
       (remove string/blank?)
       (string/join "/")
       (str "/")))

(defn uri-dir
  [& args]
  (str (apply uri args) "/"))

(defn reverse-compare [a b] (compare b a))

(defn reverse-sort-by
  [keyfn coll]
  (sort-by keyfn reverse-compare coll))

