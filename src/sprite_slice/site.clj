(ns sprite-slice.site
  (:use [ring.util.response :only [response]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.data.json :as json]))

(defroutes site-routes
  (GET "/" [] "Hello World")

  ;; test with curl -d '{"key, "yes":"ye"}' -H "Content-Type: application/json" -X POST http://localhost:5000/foo

;;   (POST "/foo" request
;;         (println
;;           (-> request
;;               :body
;;               (get-in ["keykey2"])))
;; ;;         {"zed","Uploaded user."}

;;         )

  (route/not-found "Not Found"))

(def site
  (-> site-routes
      wrap-json-body))
;;       wrap-json-response
;;       (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
