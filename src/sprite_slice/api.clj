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

(defn get-file []
  (file-response"resources/generated.zip"))

(defn get-file-ID [filename]
  (str (rand-int 1000000) "_" filename))

(defn parse-int [number-string]
  (try (Integer/parseInt number-string)
    (catch Exception e nil)))

(defroutes api-routes
  (context "/api" []
           (GET "/" [] "API HELLO WORLD")



;;download file with curl -d '{"key":"zed", "yes":"ye"}' -H "Content-Type: application/json" -X POST http://localhost:5000/api/foo --output output.zip

           (POST "/foo" request
                 (get-file))

;; curl -d '{"slug"size":"16", "columns":"2", "rows":"2", "column-spacing-size":"1", "row-spacing-size":"1"}' -H "Content-Type: application/json" -X POST http://localhost:5000/api/slice


           (POST "/slice" request
                 (let [body (:body request)
                       slug (get-in body ["slug"])
                       tile-size (get-in body ["tile-size"])
                       columns (get-in body ["columns"])
                       rows (get-in body ["rows"])
                       column-spacing-size (get-in body ["column-spacing-size"])
                       row-spacing-size (get-in body ["row-spacing-size"])]

                   (slice-image
                     {:filename (str "resources/" slug)
                      :output-location (str "generated/" slug "/")
                      :output-filename (str slug)
                      :tile-size (parse-int tile-size)
                      :columns (parse-int columns)
                      :rows (parse-int rows)
                      :column-spacing-size (parse-int column-spacing-size)
                      :row-spacing-size (parse-int row-spacing-size)
                      })
                 (json-response {:done slug})))


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
