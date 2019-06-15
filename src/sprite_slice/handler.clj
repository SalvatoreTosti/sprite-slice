(ns sprite-slice.handler
  (:require [compojure.core :refer :all]
            [sprite-slice.api :refer [api]]
            [sprite-slice.site :refer [site]]
            [ring.adapter.jetty :as jetty]))

(def app (routes api site))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty app {:port port})))
