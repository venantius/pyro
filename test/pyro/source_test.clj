(ns pyro.source-test
  (:require [clojure.test :refer :all]
            [pyro.source :as source]
            [clojure.string :as str]
            [clojure.java.io :as io]))

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

(deftest memoized-file-source-test
  (let [tempfile "test/pyro/tempfile.clj"]
    (try
      (spit tempfile "(ns pyro.tempfile)\n(def dummy1 123)\n")
      (is (str/includes? (source/memoized-file-source "pyro/tempfile.clj") "dummy1"))
      (spit tempfile "(ns pyro.tempfile)\n(def dummy2 123)\n")
      (is (str/includes? (source/memoized-file-source "pyro/tempfile.clj") "dummy2"))
      (spit tempfile "(ns pyro.tempfile)\n(def dummy3 123)\n")
      (is (str/includes? (source/memoized-file-source "pyro/tempfile.clj") "dummy3"))
      (finally
        (-> (io/file tempfile) (.delete))))))