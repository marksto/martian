(ns martian.encoding
  (:require [clojure.string :as string]))

(defn choose-content-type [encoders options]
  (some (set options) (keys encoders)))

(def auto-encoder
  {:encode identity
   :decode identity})

(defn find-encoder [encoders content-type]
  (if (string/blank? content-type)
    auto-encoder
    (loop [encoders encoders]
      (let [[encoder-content-type encoder] (first encoders)]
        (cond
          (not encoder)
          auto-encoder

          (string/includes? content-type encoder-content-type)
          encoder

          :else
          (recur (rest encoders)))))))
