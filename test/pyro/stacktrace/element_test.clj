(ns pyro.stacktrace.element-test
  (:require [clojure.test :refer :all]
            [pyro.stacktrace.element :as element]))

(deftest is-read-eval-print-element?-works
  (is (some? (element/is-read-eval-print-element?
              {:class "clojure.main/repl/read-eval-print--6625/fn--6628"})))
  (is (nil? (element/is-read-eval-print-element?
             {:class "pyro.core-test/a-test"}))))

(deftest element->map-works
  (try
    (contains? 5 5)
    (catch Exception e
      (let [element (first (.getStackTrace e))]
        (is (= (element/element->map element)
               {:class "clojure.lang.RT"
                :filename "RT.java"
                :line 724
                :method "contains"}))))))
