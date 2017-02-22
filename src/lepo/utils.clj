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
  (some->> s
           (re-find date-pattern)
           (tf/parse date-formatter)))

(defn remove-leading-slashes
  [s]
  (string/replace-first s leading-slashes-pattern ""))

(defn remove-trailing-slashes
  [s]
  (string/replace-first s trailing-slashes-pattern ""))

(defn trim-slashes
  [s]
  (-> s remove-leading-slashes remove-trailing-slashes))

(defn reverse-compare [a b] (compare b a))

(defn reverse-sort-by
  [keyfn coll]
  (sort-by keyfn reverse-compare coll))

(defn map-values
  [f coll]
  (map (fn [[k v]] [k (f v)])
       coll))
