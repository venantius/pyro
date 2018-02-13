(ns pyro.core-test
  (:require [clojure.test :refer :all]
            [pyro.dummy-fns :as dummy-fns]))

(deftest ^:demo sample-failure
  (testing "This test should deliberately fail"
    (is (some? (dummy-fns/i-dont-work)))))
