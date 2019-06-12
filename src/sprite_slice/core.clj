(ns sprite-slice.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [clojure.java.io :as io]))

(defn- get-offset [column-number spacing-size]
  (* column-number spacing-size))

(defn- get-start [number tile-size spacing-size]
  (+ (* tile-size number) (get-offset number spacing-size)))

(defn- get-tile [source-image
                 tile-size
                 column-number
                 row-number
                 column-spacing-size
                 row-spacing-size]
  (let [col-start (get-start column-number tile-size column-spacing-size)
        row-start (get-start row-number tile-size  row-spacing-size)
        img (q/create-image tile-size tile-size :rgb)]
    (q/copy source-image img [col-start row-start tile-size tile-size] [0 0 tile-size tile-size])
    img))

(defn- get-tile-row-rec [image
                         row-number
                         row-width
                         tile-size
                         column-spacing-size
                         row-spacing-size
                         accumulator
                         counter]
(let [tile-id (-> row-number
                  (* row-width)
                  (+ counter)
                  (str)
                  (keyword))
      tile (get-tile image tile-size counter row-number column-spacing-size row-spacing-size)
      accumulator (assoc accumulator tile-id tile)]
      (if (= counter (dec row-width))
        accumulator
        (get-tile-row-rec
          image row-number
          row-width
          tile-size
          column-spacing-size
          row-spacing-size
          accumulator
          (inc counter)))))

(defn- get-tile-row [image
                     row-number
                     row-width
                     tile-size
                     column-spacing-size
                     row-spacing-size]
  (let [zed
  (get-tile-row-rec image row-number row-width tile-size column-spacing-size row-spacing-size {} 0)]
    zed))

(defn- get-tile-map [source-image
                     row-count
                     row-width
                     tile-size
                     column-spacing-size
                     row-spacing-size]
  (let [zed (->> (range row-count)
       (map #(get-tile-row source-image % row-width tile-size column-spacing-size row-spacing-size))
       (into {}))]
       zed))

(def get-tiles (memoize get-tile-map))

(defn draw-image [x y img tile-size]
  (when (q/loaded? img)
    (q/image img (* x tile-size) (* y tile-size))))

(defn draw-tile
  ([x y tile-map id tile-size]
   (let [img (id tile-map)]
     (when (q/loaded? img)
       (q/image img (* x tile-size) (* y tile-size)))))
  ([x y tile-map id color tile-size]
   (draw-tile x y id tile-map)))

(defn save-image [tile-map id tile-size output-name]
  (draw-tile 0 0 tile-map id tile-size)
  (q/save (str "generated/" output-name ".png")))

(defn setup []
  (q/background 0)
  (q/frame-rate 1)
  (let [base-image (q/load-image "resources/monochrome.png")]
   (while (not (q/loaded? base-image))
      nil)
    (let [columns 3
          rows 3
          tile-size 16
          tile-map (get-tile-map
                 base-image
                 columns
                 rows
                 tile-size
                 1
                 1)
          tile-count (* columns rows)]
      (doseq [x (range tile-count)]
        (let [number-str (str x)
              k (keyword number-str)]
        (save-image tile-map k tile-size number-str))))))

(defn draw [state])

(defn zed []
(with-open [w (-> "output.gz"
                  clojure.java.io/output-stream
                  java.util.zip.GZIPOutputStream.
                  clojure.java.io/writer)]
  (binding [*out* w]
    (println "This will be compressed on disk."))))

    (zed)

(defn press-file [file]
  (with-open [w (-> "output.gz"
                  clojure.java.io/output-stream
                  java.util.zip.GZIPOutputStream.
                  clojure.java.io/writer)]
    (.write w (java.io.FileOutputStream. file))))

;;nabbed from https://stackoverflow.com/questions/17965763/zip-a-file-in-clojure
(defn zip-directory
  ([input-directory output-name]
   (with-open [zip (java.util.zip.ZipOutputStream. (io/output-stream (str output-name ".zip")))]
     (doseq [f (file-seq (io/file input-directory)) :when (.isFile f)]
       (.putNextEntry zip (java.util.zip.ZipEntry. (.getPath f)))
       (io/copy f zip)
       (.closeEntry zip))))
  ([input-directory]
   (zip-directory input-directory input-directory)))

;; (zip-directory "generated")

(q/defsketch example
  :title "image demo"
  :size [16 16]
  :setup setup
  :draw draw
  :middleware [m/fun-mode])
