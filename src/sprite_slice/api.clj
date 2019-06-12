(ns sprite-slice.api
  (:use [ring.util.response :only [response resource-response file-response]]
        [ring.util.io :only [piped-input-stream]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/hal+json; charset=utf-8"}
   :body (json/write-str data)})

(defn get-file []
  (file-response"resources/generated.zip"))

(defroutes api-routes
  (context "/api" []
           (GET "/" [] "API HELLO WORLD")

;;download file with curl -d '{"key":"zed", "yes":"ye"}' -H "Content-Type: application/json" -X POST http://localhost:5000/api/foo --output output.zip

           (POST "/foo" request
                 (get-file))
           (route/not-found "Not Found API")))


(def api
  (-> api-routes
      wrap-json-body
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
