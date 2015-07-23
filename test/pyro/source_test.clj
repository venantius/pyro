(ns pyro.source-test
  (:require [clojure.test :refer :all]
            [pyro.source :as source]))

(deftest source-fn-works
  (let [fp (:file (meta #'clojure.core/contains?))]
    (is (= (source/source-fn fp 1421 3)
           ""))))
