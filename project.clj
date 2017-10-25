(defproject lepo "0.1.0-SNAPSHOT"
  :description "Website generator for lepo.io"
  :url "https://lepo.io/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.23"]
                 [log4j/log4j "1.2.17"
                  :exclusions [javax.mail/mail
                               javax.jms/jms
                               com.sun.jmdk/jmxtools
                               com.sun.jmx/jmxri]]
                 [stasis "2.3.0"]
                 [optimus "0.19.1"]
                 [optimus-sass "0.0.3"]
                 [ring "1.5.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [clj-time "0.13.0"]
                 [selmer "1.10.6"]
                 [hickory "0.7.0"]
                 [hiccup "1.0.5"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}}
  :target-path "target/%s"
  :plugins [[lein-ring "0.10.0"]
            [lein-cloverage "1.0.9"]]
  :ring {:handler lepo.live/app
         :init lepo.live/app-init}
  :aliases {"build-site" ["run" "-m" "lepo.export/export"]
            "live" ["ring" "server-headless"]})
