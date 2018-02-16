(ns pyro.stacktrace
  (:require [clj-stacktrace.core :as st]
            [pyro.stacktrace.element :as element]))

(defn filter-repl
  "Given a seq of stacktrace element maps, take all elements until an
  element is found in `clojure.main/repl/read-eval-print`. If such a
  frame doesn't exist, just return the seq."
  {:added "0.1.0"}
  [st {:keys [drop-nrepl-elements]}]
  (if drop-nrepl-elements
    (take-while #(not (element/is-read-eval-print-element? (.getClassName %))) st)
    st))

(defn remove-clojure
  "Given a seq of stacktrace element maps, remove all elements belonging
  to the Clojure compiler or standard library (i.e. `clojure.core`,
  `clojure.lang`, etc.)"
  [st {:keys [hide-clojure-elements]}]
  (if hide-clojure-elements
    (remove #(element/is-clojure-element? (.getClassName %)) st)
    st))

(defn remove-leiningen
  "Given a seq of stacktrace element maps, remove all elements belonging
  to Leiningen (i.e. `leiningen.core.eval`, `leiningen.test`, `leiningen.core.main`"
  [st {:keys [hide-lein-elements]}]
  (if hide-lein-elements
    (remove #(element/is-lein-element? (.getClassName %)) st)
    st))

(defn clean-stacktrace
  "Clean up our stacktrace."
  [st opts]
  {:added "0.1.0"}
  (let [filtered-elements (-> st
                              (filter-repl opts)
                              (remove-clojure opts)
                              (remove-leiningen opts))]
    (map st/parse-trace-elem filtered-elements)))
