(ns martian.httpkit
  (:require [cheshire.core :as json]
            [martian.core :as martian]
            [martian.encoders :as encoders]
            [martian.file :as file]
            [martian.interceptors :as interceptors]
            [martian.openapi :as openapi]
            [martian.yaml :as yaml]
            [org.httpkit.client :as http]
            [tripod.context :as tc]))

(def go-async interceptors/remove-stack)

(def perform-request
  {:name ::perform-request
   :leave (fn [{:keys [request] :as ctx}]
            (-> ctx
                go-async
                (assoc :response
                       (http/request request
                                     (fn [response]
                                       (:response (tc/execute (assoc ctx :response response))))))))})

(def encoders
  (assoc (encoders/default-encoders)
    "multipart/form-data" {:encode encoders/multipart-encode
                           :as :multipart}))

(def default-interceptors
  (conj martian/default-interceptors
        (interceptors/encode-request encoders)
        interceptors/default-coerce-response
        perform-request))

(def default-opts {:interceptors default-interceptors})

(defn bootstrap [api-root concise-handlers & [opts]]
  (martian/bootstrap api-root concise-handlers (merge default-opts opts)))

(defn- load-definition [url load-opts]
  (or (file/local-resource url)
      @(http/get url
                 (merge {:as :text} load-opts)
                 (fn [{:keys [body]}]
                   (if (yaml/yaml-url? url)
                     (yaml/yaml->edn body)
                     (json/decode body keyword))))))

(defn bootstrap-openapi [url & [{:keys [server-url] :as opts} load-opts]]
  (let [definition (load-definition url load-opts)
        base-url (openapi/base-url url server-url definition)]
    (martian/bootstrap-openapi base-url definition (merge default-opts opts))))

(def bootstrap-swagger bootstrap-openapi)
