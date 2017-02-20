(ns lepo.augments.conf-test
  (:require [lepo.augments.conf :as conf]
            [clojure.test :refer :all]))

(deftest conf-augment
  (is (= {:root-path "/myroot/"
          :site-url "http://lepovirta.org/myroot/"
          :servlet-context "/myroot"
          :archive-uri "/posts/index.html"
          :authors-uri "/team/index.html"
          :atom-uri "/atom.xml"
          :other-stuff ["stuff"]}
         (conf/augment {:root-path "myroot"
                        :site-url "http://lepovirta.org"
                        :other-stuff ["stuff"]})))
  (is (= {:root-path "/"
          :site-url "http://lepovirta.org/"
          :servlet-context ""
          :archive-uri "/posts/index.html"
          :authors-uri "/team/index.html"
          :atom-uri "/atom.xml"
          :other-stuff ["stuff"]}
         (conf/augment {:site-url "http://lepovirta.org"
                        :other-stuff ["stuff"]}))))
