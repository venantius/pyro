(ns pyro.stacktrace.element)

(defn is-read-eval-print-element?
  "Is this stacktrace element from the read-eval-print loop?"
  {:added "0.1.0"}
  [classname]
  (re-matches #"clojure\.main\$repl\$read_eval_print.*" classname))

(defn is-clojure-element?
  "Is this stacktrace element from `clojure.core`, `clojure.lang`,
  `clojure.main`, or `clojure.test`?"
  {:added "0.1.0"}
  [classname]
  (re-matches
   #"(clojure\.core.*|clojure\.lang.*|clojure\.main.*|clojure\.test.*)"
   classname))

(defn is-lein-element?
  "Is this stacktrace element from `leiningen.core.eval`, `leiningen.test`,
  or `leiningen.core.main`?"
  {:added "0.1.0"}
  [classname]
  (re-matches
   #"(leiningen\.core\.eval.*|leiningen\.core\.main.*|leiningen\.test.*)"
   classname))

(defn matches-whitelist?
  "Does this stacktrace element match one of the regular expressions in the
  whitelist?"
  [whitelist classname]
  (re-matches
   whitelist
   classname))

(defn element->map
  "Take a stacktrace element and turn it into a map"
  {:added "0.1.0"}
  [e]
  (let [file (.getFileName e)
        clojure-fn? (and file (or (.endsWith file ".clj")
                                  (= file "NO_SOURCE_FILE")))

        classname (str (if clojure-fn?
                         (clojure.lang.Compiler/demunge (.getClassName e))
                         (.getClassName e)))]
    {:class classname
     :method (.getMethodName e)
     :filename file
     :line (.getLineNumber e)}))
