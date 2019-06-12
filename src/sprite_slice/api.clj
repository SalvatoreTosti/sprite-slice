(ns sprite-slice.api
  (:use [ring.util.response :only [response resource-response file-response]]
        [ring.util.io :only [piped-input-stream]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/hal+json; charset=utf-8"}
   :body (json/write-str data)})

(defn get-file []
  (file-response"resources/generated.zip"))

(defn get-file-ID [filename]
  (str (rand-int 1000000) "_" filename))

(defroutes api-routes
  (context "/api" []
           (GET "/" [] "API HELLO WORLD")



;;download file with curl -d '{"key":"zed", "yes":"ye"}' -H "Content-Type: application/json" -X POST http://localhost:5000/api/foo --output output.zip

           (POST "/foo" request
                 (get-file))

;;    curl -d '{"id":"1"}' -H "Content-Type: application/json" -X POST http://localhost:5000/slice

           (POST "/slice" request
                 (let [body (:body request)
                       image-id (get-in body ["id"])])
                 (json-response {:done "yep"})

                 )

;;upload file with curl -XPOST -F file=@telocalhost:5000/api/upload
           (POST "/upload"
                 {{{tempfile :tempfile filename :filename} :file} :params :as params}
                 (let [server-file-name (get-file-ID filename)]
                   (println params)
                   (io/copy tempfile (io/file "resources" "public" server-file-name))
                   (json-response {:name server-file-name})))


;;                    image-id))

           (route/not-found "Not Found API")))




(def api
  (-> api-routes
      wrap-json-body
      wrap-multipart-params
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
