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
  (map #(if (empty? (:path %))
          (get-kw-to-test %)
          (vec (flatten [(:path %) (get-kw-to-test %)])))
       (get-validation-problems spec as-map)))

(defn- get-req-keys-from-spec [spec]
  (->> (s/explain-data spec {})
       (::s/problems)
       (map :pred)
       (map last)
       (map last)))

(defn- get-matching-ns-key [ns-keys key]
  (loop [keys ns-keys]
    (if (empty? keys)
      nil
      (if (= (name (first keys)) (name key))
        (first keys)
        (recur (rest keys))))))

(defn- namespaced-map [m spec]
  (let [req-keys (get-req-keys-from-spec spec)]
    (loop [entries (seq m)
           result  {}]
      (if (empty? entries)
        result
        (let [next-entry (first entries)
              key        (first next-entry)
              value      (second next-entry)
              ns-key     (get-matching-ns-key req-keys key)]
          (recur (rest entries) (if (nil? ns-key)
                                  {}
                                  (assoc result ns-key value))))))))

(defn map->entity
  [as-map template]
  (let [ns-map       (namespaced-map as-map (:spec template))
        missing-keys (get-keywords-failing-spec ns-map (:spec template))]
    (loop [mk         missing-keys
           result-map ns-map]
      (if (empty? mk)
        result-map
        (let [k (first mk)]
          (if (not (contains? (:defaults template) k))
            (throw (Exception. (str "Error creating entity from map: "
                                    "Missing required key " k)))
            (recur (rest mk)
                   (assoc result-map
                          k
                          ((k (:defaults template)))))))))))
