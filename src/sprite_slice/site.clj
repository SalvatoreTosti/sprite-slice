(ns sprite-slice.site
  (:use [ring.util.response :only [response]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [selmer.parser :refer [render-file]]))

;; (GET "/archive.zip" []
;;     (let [archive-file (java.io.File. "c:\\home\\sample\\archive.zip")]
;;       {:status 200
;;        :body archive-file
;;        :headers {"Content-Type" "application/zip"
;;                  "Content-Length" (str (.length archive-file))
;;                  "Cache-Control" "no-cache"
;;                  "Content-Disposition" (str "attachment; filename=" (.getName archive-file))}}
;;       )
;;     )

(defroutes site-routes
  (GET "/" []
       (render-file "public/html/index.html" {}))

  (route/resources "/")

  (GET "/slicer/:slug" [slug]
;;        (response/response (io/file "lisplogo_256.png"))
;;              (response/content-type "image/png"))

       (render-file "public/html/slicer.html" {:image (str slug)}))


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
