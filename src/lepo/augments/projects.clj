(ns lepo.augments.projects)

(defn- augment-project
  [conf project]
  (assoc project
         :github-url (str (:github conf) "/" (:github-id project))))

(defn augment
  [conf projects]
  (map (partial augment-project conf)
       projects))
