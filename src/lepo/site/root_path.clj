(ns lepo.site.root-path
  (:require [lepo.uri :as uri]
            [clojure.walk]))

(def attr-names-with-uris
  #{:src :href})

(defn- kv?
  [x]
  (and (vector? x)
       (= (count x) 2)
       (keyword? (first x))))

(defn- update-uri-attr
  [root x]
  (if (kv? x)
    (let [[k v] x
          uri-attr? (attr-names-with-uris k)]
      [k (if uri-attr? (uri/add-root-path root v) v)])
    x))

(defn add-root-to-html
  [root html]
  (if (empty? root)
    html
    (clojure.walk/postwalk (partial update-uri-attr root) html)))

(defn- uri-key?
  [k]
  (.endsWith (name k) "uri"))

(defn- uri-value?
  [v]
  (string? v))

(defn update-uri-kv
  [root x]
  (if (kv? x)
    (let [[k v] x
          uri-pair? (and (uri-key? k) (uri-value? v))]
      [k (if uri-pair? (uri/add-root-path root v) v)])
    x))

(defn add-root
  ([conf]
   (if-let [root (:root-path conf)]
     (add-root root conf)
     conf))
  ([root form]
   (clojure.walk/postwalk (partial update-uri-kv root) form)))
