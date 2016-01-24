(ns lepo.utils
  (:require [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.format :as tf]))

(def slash-pattern #"^[/]*")
(def trailing-slashes-pattern #"[/]*$")
(def leading-slashes-pattern #"^[/]*")
(def date-formatter (tf/formatter "yyyy-MM-dd"))
(def date-pattern #"\d+-\d+-\d+")

(defn str->date
  [s]
  (tf/parse date-formatter (re-find date-pattern s)))

(defn trim-slashes
  [s]
  (-> s
      (string/replace-first leading-slashes-pattern "")
      (string/replace-first trailing-slashes-pattern "")))

(defn remove-file-extension
  [^java.lang.String s]
  (let [index (.lastIndexOf s ".")]
    (if (> index 0)
      (.substring s 0 index)
      s)))

(defn uri
  [& args]
  (->> args
       (map (comp trim-slashes str))
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

(defn map-values
  [f coll]
  (map (fn [[k v]] [k (f v)])
       coll))

