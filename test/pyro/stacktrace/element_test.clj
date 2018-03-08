(ns pyro.stacktrace.element-test
  (:require [clojure.test :refer :all]
            [pyro.stacktrace.element :as element]))

(deftest is-read-eval-print-element?-works
  (is (some? (element/is-read-eval-print-element?
              "clojure.main$repl$read_eval_print__6625")))
  (is (nil? (element/is-read-eval-print-element?
             "pyro.core-test/a-test"))))

(deftest matches-whitelist?-works
  (is (some? (element/matches-whitelist?
              #"(gateway\..*)"
              "gateway.middleware.auth-test/fn")))
  (is (nil? (element/matches-whitelist?
             #"(gateway\..*)"
             "common.http.client/post"))))

(deftest element->map-works
  (try
    (contains? 5 5)
    (catch Exception e
      (let [element (first (.getStackTrace e))]
        (is (= (element/element->map element)
               {:class "clojure.lang.RT"
                :filename "RT.java"
                :line 814
                :method "contains"}))))))
