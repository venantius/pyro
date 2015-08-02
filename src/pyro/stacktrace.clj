(ns pyro.stacktrace
  (:require [pyro.stacktrace.element :as element]))

(defn take-until-read-eval-print
  "Given a seq of stacktrace element maps, take all elements until an
  element is found in `clojure.main/repl/read-eval-print`. If such a
  frame doesn't exist, just return the seq."
  {:added "0.1.0"}
  [st]
  (take-while #(not (element/is-read-eval-print-element? %)) st))

(defn clean-stacktrace
  "Clean up our stacktrace."
  [st]
  {:added "0.1.0"}
  (->> st
       (map element/element->map)
       take-until-read-eval-print))
