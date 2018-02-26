(ns pyro.printer
  (:require [clj-stacktrace.repl :as repl]
            [clojure.repl]
            [clojure.stacktrace :as st]
            [pyro.source :as source]
            [pyro.stacktrace :as stacktrace]
            [pyro.stacktrace.element :as element])
  (:import [clojure.lang RT]))

(defonce options (atom nil))

(defn source-str
  [{:keys [file line]}]
  (str "(" file ":" line ")"))

(defn print-trace-element
  "Prints a Clojure-oriented view of one element in a stack trace."
  {:added "0.1.0"}
  [{:keys [class method filename line anon-fn] :as element}
   {:keys [show-source] :as opts}]
  (println (repl/method-str element) (source-str element))
  (when (and show-source (:clojure element))
    (when-let [file (source/get-var-filename element)]
      (println (source/source-fn
                file
                line 2)
               "\n"))))

(defn pprint-exception
  "Pretty-print the exception.

  Takes an optional second argument of options. Options can include the
  following keys:

    :hide-clojure-elements
    :drop-nrepl-elements

  Takes a boolean argument for whether stacktrace elements matching the
  following class names should be hidden or shown:
   * /clojure.lang*/
   * /clojure.main*/
   * /clojure.test*/

  "
  {:added "0.1.0"}
  ([] (when-let [e *e]
        (pprint-exception e)))
  ([^Throwable tr & _]
   (let [opts @options
         st (stacktrace/clean-stacktrace (.getStackTrace tr) opts)]
     (st/print-throwable tr)
     (newline)
     (print " at ")
     (if-let [e (first st)]
       (print-trace-element e opts)
       (print "[empty stack trace]"))
     (doseq [e (rest st)]
       (print "    ")
       (print-trace-element e opts)))))

(defn swap-stacktrace-engine!
  ([]
   (swap-stacktrace-engine!
    {:show-source true
     :drop-nrepl-elements true
     :hide-clojure-elements true
     :hide-lein-elements true}))
  ([opts]
   (reset! options opts)
   (alter-var-root
    #'clojure.stacktrace/print-stack-trace
    (constantly pprint-exception))
   ;; used by clojure.test
   (alter-var-root
    #'clojure.stacktrace/print-cause-trace
    (constantly pprint-exception))

   ;; REPL
   (alter-var-root
    #'clojure.main/repl-caught
    (constantly pprint-exception))
   (alter-var-root
    #'clojure.repl/pst
    (constantly pprint-exception))

   ;; Per Aviso's Pretty -- Clojure 1.8.0+ links clojure.test directly into
   ;; clojure.stacktrace, so we'll want to reload test after having modified
   ;; the variable bindings.
   (RT/loadResourceScript "clojure/test.clj")))
