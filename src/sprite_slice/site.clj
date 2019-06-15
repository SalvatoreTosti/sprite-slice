(ns sprite-slice.site
  (:use [ring.util.response :only [response]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [selmer.parser :refer [render-file]]))

(defroutes site-routes
  (route/resources "/")

  (GET "/" []
       (render-file "public/html/index.html" {}))

  (GET "/slicer/:slug" [slug]
       (render-file "public/html/slicer.html" {:image (str slug)}))

  (route/not-found "Not Found"))

(def site
  (-> site-routes
      wrap-json-body))
