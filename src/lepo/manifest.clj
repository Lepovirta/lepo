(ns lepo.manifest
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [lepo.utils :as utils]))

(def manifest-filename ".lepo.manifest")

(defn- manifest-path
  [directory]
  (io/file directory manifest-filename))

(defn- line->file
  [directory s]
  (io/file directory (utils/trim-slashes s)))

(defn- read-unsafe!
  [directory]
  (as-> directory $
        (manifest-path $)
        (slurp $)
        (string/split $ #"\n")
        (remove string/blank? $)
        (map (partial line->file directory) $)))

(defn read!
  [directory]
  (try (read-unsafe! directory)
       (catch java.io.FileNotFoundException e (log/warn "No manifest file found"))))

(defn- file->line
  [file]
  (-> file str utils/trim-slashes))

(defn- write-to-path!
  [files path]
  (->> files
       (map file->line)
       (string/join "\n")
       (spit path)))

(defn write!
  [files directory]
  (let [path (manifest-path directory)]
    (.mkdirs (io/file directory))
    (write-to-path! files path)))

(defn- delete-parent-when-empty!
  [^java.io.File file]
  (when-let [parent (.getParentFile file)]
    (when (empty? (.listFiles parent))
      (io/delete-file parent))))

(defn- delete-file!
  [^java.io.File file]
  (when-not (.isDirectory file)
    (io/delete-file file)
    (delete-parent-when-empty! file)))

(defn- delete-files!
  [files]
  (doseq [file files]
    (try (delete-file! file)
         (catch java.io.IOException e (log/warn "Failed to delete file" file)))))

(defn delete-manifest-files!
  [directory]
  (-> directory read! delete-files!))

