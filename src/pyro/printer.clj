(ns pyro.printer
  (:require [clojure.main :as main]
            [clojure.repl :as repl]
            [clojure.stacktrace :as st]
            [pyro.source :as source]
            [pyro.stacktrace :as stacktrace]
            [pyro.stacktrace.element :as element]))

clojure.core/eval

(defn print-trace-element
  "Prints a Clojure-oriented view of one element in a stack trace.

  Essentially gutted from `clojure.stacktrace`."
  {:added "0.1.0"}
  [{:keys [class method filename line] :as element}
   {:keys [hide-clojure-elements
           hide-nrepl-elements
           show-source] :as opts}]
  (if (= "invoke" method)
    (printf "%s" class)
    (printf "%s.%s" class method))
  (printf " (%s:%d)" filename line)
  (newline)
  (when show-source
    (when-let [file (source/get-var-filename class)]
      (println (source/source-fn (source/get-var-filename class) line 2)))))

(defn pprint-exception
  "Pretty-print the exception.

  Takes an optional second argument of options. Options can include the
  following keys:

    :hide-clojure-elements
    :hide-nrepl-elements

  Takes a boolean argument for whether stacktrace elements matching the
  following class names should be hidden or shown:
   * /clojure.lang*/
   * /clojure.main*/
   * /clojure.test*/

  "
  {:added "0.1.0"}
  ([tr] (pprint-exception {} tr))
  ([opts ^Throwable tr & _]
   (let [st (stacktrace/clean-stacktrace (.getStackTrace tr))]
     (st/print-throwable tr)
     (newline)
     (print " at ")
     (if-let [e (first st)]
       (print-trace-element e opts)
       (print "[empty stack trace]"))
     (newline)
     (doseq [e (rest st)]
       (print "    ")
       (print-trace-element e opts)))))

(defn middleware
  []

  (alter-var-root
   #'st/print-stack-trace
   (constantly (partial pprint-exception {})))
  (alter-var-root
   #'st/print-cause-trace ;; this is what clojure.test uses
   (constantly (partial pprint-exception {:show-source true})))

;; REPL
  (alter-var-root
   #'main/repl-caught
   (constantly (partial pprint-exception {:show-source true})))
  (alter-var-root
   #'repl/pst
   (constantly (partial pprint-exception {:show-source true}))))
