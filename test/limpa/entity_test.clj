(ns limpa.entity-test
  (:require [clojure.pprint :refer [pprint]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [limpa.entity :as sut]))

(s/def ::req-key boolean?)
(s/def ::opt-key string?)
(s/def ::test-spec (s/keys :req [::sut/id
                                 ::req-key
                                 ::opt-key]))

(def test-template {:spec     ::test-spec
                    :defaults {::sut/id  sut/make-uuid
                               ::opt-key (fn [] "Generated")}})


(s/def ::parent-req-key boolean?)
(s/def ::parent-opt-key string?)
(s/def ::nested-spec ::test-spec)
(s/def ::parent-test-spec (s/keys :req [::parent-req-key
                                        ::parent-opt-key
                                        ::nested-spec]))

(def parent-test-template {:spec     ::parent-test-spec
                           :defaults {::sut/id         sut/make-uuid
                                      ::parent-opt-key (fn [] "Generated")}})

(deftest map->entity_namespaced-key
  (let [as-map   {::req-key true}
        template test-template
        result   (sut/map->entity as-map template)]
    (is (= "Generated" (::opt-key result)))
    (is (::req-key result))
    (is (uuid? (::sut/id result)))))

(deftest map->entity_not-namespaced-key
  (let [as-map   {:req-key true}
        template test-template
        result   (sut/map->entity as-map template)]
    (is (= "Generated" (::opt-key result)))
    (is (::req-key result))
    (is (uuid? (::sut/id result)))))

(deftest map->entity_nested
  (let [as-map   {::parent-req-key true
                  ::nested-spec    {::sut/id  (sut/make-uuid)
                                    ::req-key true
                                    ::opt-key "Provided"}}
        template parent-test-template
        result   (sut/map->entity as-map template)]
    (is (= "Generated" (::parent-opt-key result)))
    (is (s/valid? ::test-spec (::nested-spec result)))))

(deftest map->entity_nested-partial
  (let [as-map   {::parent-req-key true
                  ::nested-spec    {::req-key true}}
        template parent-test-template
        result   (sut/map->entity as-map template)]
    (is (= "Generated" (::parent-opt-key result)))))
