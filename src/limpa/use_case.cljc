(ns limpa.use-case
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s]))
  (:import clojure.lang.IExceptionInfo))

;; Part of this is blatantly copied from brianium's excellent clean-todos project:
;; https://github.com/brianium/clean-todos

(s/def ::success? boolean?)
(s/def ::message string?)
(s/def ::error #{::unknown-error ::entity-not-found ::no-connection
                 ::spec-failed})
(s/def ::details any?)
(s/def ::payload any?)

(s/def ::result (s/keys :req [::success?
                              ::message]
                        :opt [::error
                              ::payload]))

(defn make-success
  ([message]
   {::success? true
    ::message  message})
  ([message payload]
   (merge (make-success message)
          {::payload payload})))

(defn make-error
  ([message error]
   {::success? false
    ::message  message
    ::error    error})
  ([message error payload]
   (merge (make-error message error)
          {::payload payload})))

(defn exception->error
  "Converts an exception into an error DTO object"
  [e]
  (make-error (.getMessage e)
              (or (-> e (ex-data) (:error-code)) ::unknown-error)
              "Stack traces currently disabled"))

(defmacro defusecase
  [name args & body]
  `(def ~name (fn ~args
                (try
                  ~@body
                  (catch #?(:clj Exception :cljs js/Error) e#
                    (exception->error e#))))))
