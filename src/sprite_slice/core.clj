(ns sprite-slice.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

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

(defn setup []
  (q/background 0)
  (q/frame-rate 1)
  (let [base-image (q/load-image "resources/monochrome.png")]
   (while (not (q/loaded? base-image))
      nil)
;;      (let [tile (get-tile base-image 16 31 9 1 1)]
;;        (draw-image 0 0 tile)
;;        (zed))
      {:img base-image}))

(defn draw [state]
  (let[tile-map (get-tile-map
                  (:img state)
                  3
                  3
                  16
                  1
                  1)]
    (draw-tile 0 0 tile-map :8 16)
  ))

(defn zed []
(q/camera 16 16 16 0 0 0 0 0 1)
(q/save "generated/box.png"))


(q/defsketch example
  :title "image demo"
  :size [(* 16 16) (* 16 16)]
  :setup setup
  :draw draw
  :middleware [m/fun-mode])
