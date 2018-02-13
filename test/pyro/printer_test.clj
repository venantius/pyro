(ns pyro.printer-test
  (:require [clojure.test :refer :all]
            [pyro.printer :as printer]))

(deftest source-str-works
  (is (= (printer/source-str {:file "printer_test.clj"
                              :line 7})
         "(printer_test.clj:7)")))
