(ns pyro.source-test
  (:require [clojure.test :refer :all]
            [glow.core :as glow]
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
                                   :fn "sample-failure"
                                   :file "core_test.clj"})
         "pyro/core_test.clj")))

;; I don't feel like doing this right now
#_(deftest source-fn-works
    (let [fp (:file (meta #'clojure.core/contains?))]
      (with-redefs [glow/highlight identity]
        (is (= (source/source-fn fp 1421 3)
               "      it will not perform a linear search for a value.  See also 'some'.\"\n      {:added \"1.0\"\n       :static true}\n-->   [coll key] (. clojure.lang.RT (contains coll key)))\n    \n    (defn get\n      \"Returns the value mapped to key, not-found or nil if key not present.\"")))))
