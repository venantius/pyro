(ns pyro.printer
  (:require [clj-stacktrace.repl :as repl]
            [clojure.repl]
            [clojure.stacktrace :as st]
            [pyro.source :as source]
            [pyro.stacktrace :as stacktrace]
            [pyro.stacktrace.element :as element]))


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
    (when-let [file (source/get-var-filename (:ns element) (:fn element))]
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
  ([tr] (pprint-exception {} tr))
  ([opts ^Throwable tr & _]
   (let [st (stacktrace/clean-stacktrace (.getStackTrace tr) opts)]
     (st/print-throwable tr)
     (newline)
     (print " at ")
     (if-let [e (first st)]
       (print-trace-element e opts)
       (print "[empty stack trace]"))
     (doseq [e (rest st)]
       (print "    ")
       (print-trace-element e opts)))))

(defn middleware
  []
  (alter-var-root
   #'clojure.stacktrace/print-stack-trace
   (constantly (partial pprint-exception {})))
  (alter-var-root
   #'clojure.stacktrace/print-cause-trace ;; this is what clojure.test uses
   (constantly (partial pprint-exception {:show-source true
                                          :drop-nrepl-elements true
                                          :hide-clojure-elements true})))

;; REPL
  (alter-var-root
   #'clojure.main/repl-caught
   (constantly (partial pprint-exception {:show-source true
                                          :drop-nrepl-elements true
                                          :hide-clojure-elements true})))
  (alter-var-root
   #'clojure.repl/pst
   (constantly (partial pprint-exception {:show-source true
                                          :drop-nrepl-elements true
                                          :hide-clojure-elements true}))))
