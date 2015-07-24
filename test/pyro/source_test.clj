(ns pyro.source-test
  (:require [clojure.test :refer :all]
            [pyro.source :as source]))

(deftest source-fn-works
  (let [fp (:file (meta #'clojure.core/contains?))]
    (is (= (source/source-fn fp 1421 3)
           "      it will not perform a linear search for a value.  See also 'some'.\"\n      {:added \"1.0\"\n       :static true}\n-->   [coll key] (. clojure.lang.RT (contains coll key)))\n    \n    (defn get\n      \"Returns the value mapped to key, not-found or nil if key not present.\""))))
