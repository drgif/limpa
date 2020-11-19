(ns limpa.use-case-test
  (:require [limpa.use-case :as sut]
            [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(deftest make-success-test
  (let [msg    "That was successful"
        result (sut/make-success msg)]
    (is (s/valid? ::sut/result result))
    (is (true? (::sut/success? result)))
    (is (= msg (::sut/message result)))
    (is (nil? (::sut/error result))))
  (let [msg     "Successful again"
        payload {:some-key "some data"}
        result  (sut/make-success msg payload)]
    (is (s/valid? ::sut/result result))
    (is (true? (::sut/success? result)))
    (is (= msg (::sut/message result)))
    (is (nil? (::sut/error result)))
    (is (= payload (::sut/payload result)))))

(deftest make-error-test
  (let [msg    "There was an error"
        error  ::sut/unknown-error
        result (sut/make-error msg error)]
    (is (s/valid? ::sut/result result))
    (is (false? (::sut/success? result)))
    (is (= msg (::sut/message result)))
    (is (= error (::sut/error result))))
  (let [msg     "There was an error"
        payload {:some-key "some data"}
        error   ::sut/unknown-error
        result  (sut/make-error msg error payload)]
    (is (s/valid? ::sut/result result))
    (is (false? (::sut/success? result)))
    (is (= msg (::sut/message result)))
    (is (= error (::sut/error result)))
    (is (= payload (::sut/payload result)))))
