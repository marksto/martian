(ns martian.multipart
  "An adaptor that prepares multipart content for all supported HTTP clients"
  (:require #?(:clj [clojure.java.io :as io]))
  #?(:clj (:import (java.io File InputStream))))

;; http-kit       = {String | File, InputStream, byte[] | ByteBuffer! | ~Number!~}
;; clj-http       = {String | File, InputStream, byte[] | o.a.h.e.m.c.ContentBody}
;; hato           = {String | File, InputStream, byte[] | URL, URI, Socket, Path!}
;; bb/http-client = {String | File, InputStream, byte[] | URL, URI, Socket, Path?}
;; clj-http-lite  — no support for multipart content
;; TODO: Add multipart support for JS HTTP clients.

(defn common-binary? [obj]
  #?(:clj (or (instance? File obj)
              (instance? InputStream obj)
              (bytes? obj))))

#?(:bb nil
   :clj
   (def default-make-input-stream-impl
     (:make-input-stream io/default-streams-impl)))

#?(:bb nil
   :clj
   (defn implements-make-input-stream? [obj]
     (not= default-make-input-stream-impl
           (find-protocol-method io/IOFactory :make-input-stream obj))))

(defn coerce-content
  ([obj]
   (coerce-content obj nil))
  ([obj pass-pred]
   (when (some? obj) ; let it fail downstream
     (cond
       (or (string? obj)
           (common-binary? obj)
           (and pass-pred (pass-pred obj)))
       obj

       #?@(:bb
           [:else
            (try
              (io/input-stream obj)
              (catch Exception _ex
                (str obj)))]

           :clj
           [(implements-make-input-stream? obj)
            (io/input-stream obj)

            :else
            (str obj)])))))
