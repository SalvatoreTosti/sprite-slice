(ns sprite-slice.api
  (:use [ring.util.response :only [response resource-response file-response]]
        [ring.util.io :only [piped-input-stream]]
        [sprite-slice.core :only [slice-image]])
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

(defn get-file-ID []
  (str (rand-int 1000000)))

(defn parse-int [number-string]
  (try (Integer/parseInt number-string)
    (catch Exception e nil)))

(defn upload-file [request]
  (let [params (:params request)
        filename (get-in params [:file :filename])
        tempfile (get-in params [:file :tempfile])
        slug (get-file-ID)]
    (io/copy tempfile (io/file "uploads" slug))
    {:slug slug}))

(defn slice-file
  ([request slug]
  (let [body (:body request)
        tile-size (get-in body ["tile-size"])
        columns (get-in body ["columns"])
        rows (get-in body ["rows"])
        column-spacing-size (get-in body ["column-spacing-size"])
        row-spacing-size (get-in body ["row-spacing-size"])]
    (slice-image
      {:filename (str "uploads/" slug)
       :output-location (str "generated/" slug "/")
       :output-filename (str slug)
       :tile-size (parse-int tile-size)
       :columns (parse-int columns)
       :rows (parse-int rows)
       :column-spacing-size (parse-int column-spacing-size)
       :row-spacing-size (parse-int row-spacing-size)})
    {:slug slug}))
  ([request]
   (slice-file request (get-in request [:body "slug"]))))

(defn get-zip-filename [request]
  (-> request
      (get-in [:body "slug"])
      (#(str "output/" % ".zip"))))

(defroutes api-routes
  (context "/api" []
;;            (POST "/run" request
;;                  (let [slug (upload-file request)]))

;;curl -d '{"slug":"123"}' -H "Content-Type: application/json" -X POST http://localhost:5000/api/fetch --output output.zip

           (POST "/fetch" request
                 (-> (get-zip-filename request)
                     (file-response)))

;; curl -d '{"slug":"123", "tile-size":"16", "columns":"2", "rows":"2", "column-spacing-size":"1", "row-spacing-size":"1"}' -H "Content-Type: application/json" -X POST http://localhost:5000/api/slice

           (POST "/slice" request
                 (-> (slice-file request)
                     (json-response)))

;;upload file with curl -XPOST -F file=@monochrome.png localhost:5000/api/upload
           (POST "/upload" request
                 (-> (upload-file request)
                     (json-response)))

           (route/not-found "Not Found API")))

(def api
  (-> api-routes
      wrap-json-body
      wrap-multipart-params
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
