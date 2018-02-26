(ns pyro.core-test
  (:require [clojure.test :refer :all]
            [pyro.dummy-fns :as dummy-fns]))

(def sample-var)

(deftest ^:demo sample-failure
  (is (some? (dummy-fns/i-dont-work)))
  "this
  is
  :a")
