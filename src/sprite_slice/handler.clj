(ns sprite-slice.handler
  (:require [compojure.core :refer :all]
            [sprite-slice.api :refer [api]]
            [sprite-slice.site :refer [site]]))

(def app (routes api site))
