(ns sprite-slice.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [clojure.java.io :as io]))

(defn- get-offset [column-number spacing-size]
  (* column-number spacing-size))

(defn- get-start [number tile-size spacing-size]
  (+ (* tile-size number) (get-offset number spacing-size)))

(defn- get-tile [source-image
                 column-number
                 row-number
                 {:keys [tile-size column-spacing-size row-spacing-size] :as args}]
  (let [col-start (get-start column-number tile-size column-spacing-size)
        row-start (get-start row-number tile-size row-spacing-size)
        img (q/create-image tile-size tile-size :rgb)]
    (q/copy source-image img [col-start row-start tile-size tile-size] [0 0 tile-size tile-size])
    img))

(defn- get-tile-row-rec [image
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
      (get-tile-row-rec image row-number args accumulator (inc counter)))))

(defn- get-tile-row [image
                     row-number
                     {:keys [tile-size columns column-spacing-size row-spacing-size] :as args}]
  (get-tile-row-rec image row-number args {} 0))

(defn- get-tile-map [source-image
                     {:keys [tile-size columns rows column-spacing-size row-spacing-size] :as args}]
  (->> (range rows)
    (map #(get-tile-row source-image % args))
    (into {})))

;; (def get-tiles (memoize get-tile-map))

(defn- draw-image [x y img tile-size]
  (when (q/loaded? img)
    (q/image img (* x tile-size) (* y tile-size))))

(defn- draw-tile
  ([x y tile-map id tile-size]
   (let [img (id tile-map)]
     (when (q/loaded? img)
       (q/image img (* x tile-size) (* y tile-size))))))

(defn- save-image [tile-map id tile-size output-name]
  (draw-tile 0 0 tile-map id tile-size)
  (q/save (str "generated/" output-name ".png")))

;;nabbed from https://stackoverflow.com/questions/17965763/zip-a-file-in-clojure
(defn- zip-directory
  ([input-directory output-name]
   (with-open [zip (java.util.zip.ZipOutputStream. (io/output-stream (str output-name ".zip")))]
     (doseq [f (file-seq (io/file input-directory)) :when (.isFile f)]
       (.putNextEntry zip (java.util.zip.ZipEntry. (.getPath f)))
       (io/copy f zip)
       (.closeEntry zip))))
  ([input-directory]
   (zip-directory input-directory input-directory)))

(defn- setup [{:keys [filename
                     tile-size
                     columns
                     rows
                     column-spacing-size
                     row-spacing-size] :as args}]
  (q/background 0)
  (q/frame-rate 1)
  (let [base-image (q/load-image filename)]
   (while (not (q/loaded? base-image))
      nil)
    (let [tile-map (get-tile-map base-image args)
          tile-count (* columns rows)]
      (doseq [x (range tile-count)]
        (let [number-str (str x)
              k (keyword number-str)]
        (save-image tile-map k tile-size number-str)))
      (zip-directory "generated" "test-auto-zip2")
      )))

(defn- draw [state]
  (q/exit))

(defn slice-image [{:keys [tile-size] :as args}]
  (q/defsketch example
    :size [tile-size tile-size]
    :setup (fn [] (setup args))
    :draw draw
    :middleware [m/fun-mode]))

(slice-image
  {:filename "resources/monochrome.png"
   :tile-size 16
   :columns 5
   :rows 2
   :column-spacing-size 1
   :row-spacing-size 1})
