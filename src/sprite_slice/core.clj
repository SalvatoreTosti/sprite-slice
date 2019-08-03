(ns sprite-slice.core
  (:import [javax.imageio ImageIO])
  (:import [java.io File])
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [clojure.java.io :as io]))

(defn- get-offset [column-number spacing-size]
  (* column-number spacing-size))

(defn- get-start [number tile-size spacing-size]
  (+ (* tile-size number) (get-offset number spacing-size)))

(defn copy-image [source-image [x-start y-start] [x-end y-end]]
  (let [copy-img (java.awt.image.BufferedImage.
                   (- x-end x-start)
                   (- y-end y-start)
                   java.awt.image.BufferedImage/TYPE_INT_ARGB)]
    (doseq [x (range x-start x-end)
            y (range y-start y-end)]
            (.setRGB
              copy-img
              (- x x-start)
              (- y y-start)
              (.getRGB source-image x y)))
    copy-img))

(defn- get-tile [source-image
                 column-number
                 row-number
                 {:keys [tile-size column-spacing-size row-spacing-size] :as args}]
  (let [col-start (get-start column-number tile-size column-spacing-size)
        row-start (get-start row-number tile-size row-spacing-size)]
    (copy-image source-image [col-start row-start] [(+ col-start tile-size) (+ row-start tile-size)])))

(defn- get-tile-row
  ([image
    row-number
    {:keys [tile-size columns column-spacing-size row-spacing-size] :as args}
    accumulator
    counter]
   (let [tile-id (-> row-number
                    (* columns)
                    (+ counter)
                    (str)
                    (keyword))
        tile (get-tile image counter row-number args)
        accumulator (assoc accumulator tile-id tile)]
    (if (= counter (dec columns))
      accumulator
      (get-tile-row image row-number args accumulator (inc counter)))))
  ([image
    row-number
    {:keys [tile-size columns column-spacing-size row-spacing-size] :as args}]
   (get-tile-row image row-number args {} 0)))

(defn- get-tile-map [source-image
                     {:keys [tile-size columns rows column-spacing-size row-spacing-size] :as args}]
  (->> (range rows)
       (map #(get-tile-row source-image % args))
       (into {})))

(defn- draw-image [x y img tile-size]
  (when (q/loaded? img)
    (q/image img (* x tile-size) (* y tile-size))))

(defn- draw-tile
  ([x y tile-map id tile-size]
   (let [img (id tile-map)]
     (when (q/loaded? img)
       (q/image img (* x tile-size) (* y tile-size))))))

(defn- save-image [tile-map id tile-size output-name {:keys [output-location] :as args}]
  (draw-tile 0 0 tile-map id tile-size)
  (q/save (str output-location output-name ".png")))

;;nabbed from https://stackoverflow.com/questions/17965763/zip-a-file-in-clojure
(defn- zip-directory
  ([input-directory output-directory output-name]
   (.mkdir (java.io.File. output-directory))
   (with-open [zip (java.util.zip.ZipOutputStream. (io/output-stream (str output-directory "/" output-name ".zip")))]
     (doseq [f (file-seq (io/file input-directory)) :when (.isFile f)]
       (.putNextEntry zip (java.util.zip.ZipEntry. (.getPath f)))
       (io/copy f zip)
       (.closeEntry zip))))
  ([input-directory]
   (zip-directory input-directory input-directory)))

(defn save-image [img full-path]
  (ImageIO/write img "png" (clojure.java.io/file full-path)))

(defn save-entry [[k v] directory]
  (save-image v (str directory "/" (name k) ".png")))

(defn slice-image [{:keys
                    [filename
                     tile-size
                     columns
                     rows
                     column-spacing-size
                     row-spacing-size
                     output-location] :as args}]
    (let [img (ImageIO/read (File. filename))
          image-entries (-> img
                            (get-tile-map args)
                            seq)]
          (doseq [entry image-entries]
            (save-entry entry output-location))))

(defn run! [{:keys
             [filename
              tile-size
              columns
              rows
              column-spacing-size
              row-spacing-size
              output-location
              output-filename] :as args}]
  (.mkdir (java.io.File. output-location))
  (slice-image args)
  (zip-directory output-location "output" output-filename))
