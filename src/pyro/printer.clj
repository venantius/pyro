(ns pyro.printer
  (:require [clojure.main :as main]
            [clojure.repl :as repl]
            [clojure.stacktrace :as st]
            [pyro.stacktrace :as stacktrace]
            [pyro.stacktrace.element :as element]))

(defn print-trace-element
  "Prints a Clojure-oriented view of one element in a stack trace.

  Essentially gutted from `clojure.stacktrace`."
  {:added "0.1.0"}
  [{:keys [class method filename line] :as element}]
  (if (= "invoke" method)
    (printf "%s" class)
    (printf "%s.%s" class method))
  (printf " (%s:%d)" filename line))

(defn pprint-exception
  "Pretty-print the exception.

  Takes an optional second argument of options. Options can include the
  following keys:

    :hide-clojure-elements

  Takes a boolean argument for whether stacktrace elements matching the
  following class names should be hidden or shown:
   * /clojure.lang*/
   * /clojure.main*/

  "
  ([tr] (pprint-exception tr {}))
  ([^Throwable tr opts]
   (let [st (stacktrace/clean-stacktrace (.getStackTrace tr))]
     (st/print-throwable tr)
     (newline)
     (print " at ")
     (if-let [e (first st)]
       (print-trace-element e)
       (print "[empty stack trace]"))
     (newline)
     (doseq [e (rest st)]
       (print "    ")
       (print-trace-element e)
       (newline)))))

(defn middleware
  []

  (alter-var-root
   #'st/print-stack-trace
   (constantly pprint-exception))
  (alter-var-root
   #'st/print-cause-trace
   (constantly pprint-exception))

;; REPL
  (alter-var-root
   #'main/repl-caught
   (constantly pprint-exception))
  (alter-var-root
   #'repl/pst
   (constantly pprint-exception)))
