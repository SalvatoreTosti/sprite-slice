(defproject sprite-slice "0.1.0-SNAPSHOT"
  :description "A sprite-splitting web app."
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [quil "3.0.0"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [selmer "1.12.12"]
                 [ring "1.7.1"]]
  :main sprite-slice.handler
  :aot [sprite-slice.handler]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler sprite-slice.handler/app
         :port 5000}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
