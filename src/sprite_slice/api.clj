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

(defn- json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/hal+json; charset=utf-8"}
   :body (json/write-str data)})

(defn- get-file-ID []
  (str (java.util.UUID/randomUUID)))

(defn- parse-int [number-string]
  (try (Integer/parseInt number-string)
    (catch Exception e nil)))

(defn- upload-file [request]
  (let [params (:params request)
        filename (get-in params [:file :filename])
        tempfile (get-in params [:file :tempfile])
        slug (get-file-ID)]
    (io/copy tempfile (io/file "uploads" slug))
    {:slug slug}))

(defn- slice-file
  ([request slug]
  (let [params (:params request)
        tile-size (get-in params [:tile-size])
        columns (get-in params [:columns])
        rows (get-in params [:rows])
        column-spacing-size (get-in params [:column-spacing-size])
        row-spacing-size (get-in params [:row-spacing-size])]
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

(defn- slug-to-zip-name [slug]
  (str "output/" slug ".zip"))

(defn- get-zip-filename [request]
  (-> request
      (get-in [:body "slug"])
      (slug-to-zip-name)))

(defn- transmit-zip-file [path]
  (let [file (java.io.File. path)]
    {:status 200
     :body file
     :headers {"Content-Type" "application/zip"
               "Content-Length" (str (.length file))
               "Cache-Control" "no-cache"
               "Content-Disposition" (str "attachment; filename="(.getName file))}}))

(defn initialize-directories []
  (.mkdir (io/file "uploads"))
  (.mkdir (io/file "output"))
  (.mkdir (io/file "generated")))

(defroutes api-routes
  (context "/api" []
           (POST "/run" request
                 (initialize-directories)
                 (let [slug (upload-file request)
                       slug (slice-file request (:slug slug))
                       zip-name (slug-to-zip-name (:slug slug))]
                   (while (not (file-response zip-name))
                       (Thread/sleep 1000))
                   (transmit-zip-file zip-name)))

           (GET "/display" request
                (initialize-directories)
                (-> (file-response "uploads/74848.png")))

           ;;upload file with curl -XPOST -F file=@monochrome.png localhost:5000/api/upload
           ;;curl -XPOST -d '{"file" : "@monochrome.png"}' localhost:5000/api/test

           (POST "/upload" request
                 (initialize-directories)
                 (-> (upload-file request)
                     (json-response)))

;; curl -d '{"slug":"123", "tile-size":"16", "columns":"2", "rows":"2", "column-spacing-size":"1", "row-spacing-size":"1"}' -H "Content-Type: application/json" -X POST http://localhost:5000/api/slice

           (POST "/slice" request
                 (initialize-directories)
                 (-> (slice-file request)
                     (json-response)))

;;curl -d '{"slug":"123"}' -H "Content-Type: application/json" -X POST http://localhost:5000/api/fetch --output output.zip

           (POST "/fetch" request
                 (initialize-directories)
                 (-> (get-zip-filename request)
                     (file-response)))

           (route/not-found "Not Found API")))

(def api
  (-> api-routes
      wrap-json-body
      wrap-multipart-params
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
