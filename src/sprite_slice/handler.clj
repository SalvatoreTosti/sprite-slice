(ns sprite-slice.handler
  (:use [ring.util.response :only [response]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.params :refer [wrap-params]]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (POST "/foo" request
        (println (:body request))
        (response "Uploaded user."))

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-body
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
