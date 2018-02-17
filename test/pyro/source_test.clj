(ns pyro.source-test
  (:require [clojure.test :refer :all]
            [pyro.source :as source]))

(deftest pad-integer-test
  (is (= (source/pad-integer 5) "5   "))
  (is (= (source/pad-integer 532) "532 ")))

(deftest pad-source-test
  (is (= (source/pad-source "(+ 1 1)" 30)
         "    30   (+ 1 1)")))

(deftest pad-source-arrow-test
  (is (= (source/pad-source-arrow "(+ 1 1)" 30)
         "--> 30   (+ 1 1)")))

(deftest ns->filename-test
  (is (= (source/ns->filename "pyro.core-test" "core_test.clj")
         "pyro/core_test.clj")))

(deftest get-var-filename-test
  (is (= (source/get-var-filename {:ns "pyro.core-test"
                                   :fn "fn"
                                   :file "core_test.clj"})
         "pyro/core_test.clj"))
  (is (= (source/get-var-filename {:ns "pyro.core-test"
                                   :fn "sample-var"
                                   :file "core_test.clj"})
         "pyro/core_test.clj")))
