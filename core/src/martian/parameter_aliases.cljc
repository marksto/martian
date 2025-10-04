(ns martian.parameter-aliases
  (:require [schema.core :as s]
            [camel-snake-kebab.core :refer [->kebab-case]]
            [clojure.set :refer [rename-keys]]
            [martian.schema-tools :refer [unspecify-key key-seqs prewalk-with-path]]))

;; TODO: Lean on `schema-tools.core` for some of these transformations.

(defn can-be-kebabised? [k]
  (not (and (keyword? k) (namespace k))))

(defn ->idiomatic [k]
  (when-some [uk (when k (unspecify-key k))]
    (when (can-be-kebabised? uk)
      (->kebab-case uk))))

(defn- idiomatic-path [path]
  (vec (keep ->idiomatic path)))

(defn parameter-aliases
  "Produces a data structure for use with `unalias-data`"
  [schema]
  (reduce (fn [acc path]
            (if-let [idiomatic-key (some-> path last ->idiomatic)]
              (if-not (= (last path) idiomatic-key)
                (update acc (idiomatic-path (drop-last path)) merge {idiomatic-key (last path)})
                acc)
              acc))
          {}
          (key-seqs schema)))

(defn unalias-data
  "Takes parameter aliases and (deeply nested) data, returning data with deeply-nested keys renamed as described by parameter-aliases"
  [parameter-aliases x]
  (if parameter-aliases
    (prewalk-with-path (fn [path x]
                         (if (map? x)
                           (rename-keys x (get parameter-aliases (idiomatic-path path)))
                           x))
                       []
                       x)
    x))

(defn alias-schema
  "Walks a schema, transforming all keys into their aliases (idiomatic keys)"
  [parameter-aliases schema]
  (if parameter-aliases
    (prewalk-with-path (fn [path x]
                         (if (map? x)
                           (let [kmap (reduce-kv (fn [kmap k v]
                                                   (assoc kmap v k (s/optional-key v) (s/optional-key k)))
                                                 {}
                                                 (get parameter-aliases (idiomatic-path path)))]
                             (rename-keys x kmap))
                           x))
                       []
                       schema)
    schema))
