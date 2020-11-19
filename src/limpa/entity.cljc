(ns limpa.entity
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])))

;; Part of this is blatantly copied from brianium's excellent clean-todos project:
;; https://github.com/brianium/clean-todos

(s/def ::id uuid?)

(def uuid-regexp #"(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")

(defn uuid-string?
  "Check if the given string conforms to a UUID v1 - 5 format"
  [str]
  (string? (re-matches uuid-regexp str)))

(defn make-uuid
  "Creates a new uuid"
  []
  #?(:clj (java.util.UUID/randomUUID)
     :cljs (cljs.core/random-uuid)))

(defn string->uuid
  "Converts a uuid string to a uuid"
  [str]
  #?(:clj (java.util.UUID/fromString str)
     :cljs (cljs.core/uuid str)))

(defn date
  "Creates a date in a target agnostic way"
  []
  #?(:clj (java.util.Date.)
     :cljs (js/Date.)))

(defn- get-validation-problems
  [spec sut]
  (-> (s/explain-data spec sut)
      (get ::s/problems)))

(defn- third [coll]
  (nth coll 2))

(defn- get-kw-if-checked-for [exp]
  (if (and (= (first exp) 'clojure.core/contains?)
           (keyword? (third exp)))
    (third exp)))

(defn- get-kw-to-test
  [problem]
  (-> problem
      (:pred)
      (third)
      (get-kw-if-checked-for)))

(defn- get-keywords-failing-spec
  [as-map spec]
  (map #(get-kw-to-test %) (get-validation-problems spec as-map)))

(defn map->entity
  ( [as-map spec defaults]
   (let [missing-keys (get-keywords-failing-spec as-map spec)]
     (loop [mk         missing-keys
            result-map as-map]
       (if (empty? mk)
         result-map
         (let [k (first mk)]
           (if (not (contains? defaults k))
             (throw (Exception. (str "Missing key " k)))
             (recur (rest mk)
                    (assoc result-map
                           k
                           ((k defaults))))))))))
  ([m {:keys [spec defaults]}]
   (map->entity m spec defaults)))
