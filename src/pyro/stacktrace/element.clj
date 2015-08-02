(ns pyro.stacktrace.element)

(defn is-read-eval-print-element?
  "Is this stacktrace element from the read-eval-print loop?"
  {:added "0.1.0"}
  [e]
  (re-matches #"clojure\.main/repl/read-eval-print.*" (:class e)))

(defn element->map
  "Take a stacktrace element and turn it into a map"
  {:added "0.1.0"}
  [e]
  (let [file (.getFileName e)
        clojure-fn? (and file (or (.endsWith file ".clj")
                                  (= file "NO_SOURCE_FILE")))]
    {:class (str (if clojure-fn?
                   (clojure.lang.Compiler/demunge (.getClassName e))
                   (.getClassName e)))
     :method (.getMethodName e)
     :filename file
     :line (.getLineNumber e)}))
