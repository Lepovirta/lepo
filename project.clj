(defproject lepo "0.1.0-SNAPSHOT"
  :description "Website generator for lepo.io"
  :url "https://lepo.io/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [log4j/log4j "1.2.17"
                  :exclusions [javax.mail/mail
                               javax.jms/jms
                               com.sun.jmdk/jmxtools
                               com.sun.jmx/jmxri]]
                 [stasis "2.3.0"]
                 [optimus "0.18.5"]
                 [optimus-sass "0.0.3"]
                 [ring "1.4.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [clj-time "0.11.0"]
                 [selmer "1.0.4"]]
  :target-path "target/%s"
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler lepo.live/app
         :init lepo.live/app-init}
  :aliases {"build-site" ["run" "-m" "lepo.export/export"]})
