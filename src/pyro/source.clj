(ns pyro.source
  "A namespace for reading source code off of the classpath"
  (:import [java.io LineNumberReader InputStreamReader PushbackReader]
           [clojure.lang RT]))

#_(defn source-fn
    "Returns a string of the source code for the given symbol, if it can
  find it.  This requires that the symbol resolve to a Var defined in
  a namespace for which the .clj is in the classpath.  Returns nil if
  it can't find the source.  For most REPL usage, 'source' is more
  convenient.
  Example: (source-fn 'filter)"
    [x]
    (when-let [v (resolve x)]
      (when-let [filepath (:file (meta v))]
        (when-let [strm (.getResourceAsStream (RT/baseLoader) filepath)]
          (with-open [rdr (LineNumberReader. (InputStreamReader. strm))]
            (dotimes [_ (dec (:line (meta v)))] (.readLine rdr))
            (let [text (StringBuilder.)
                  pbr (proxy [PushbackReader] [rdr]
                        (read [] (let [i (proxy-super read)]
                                   (.append text (char i))
                                   i)))
                  read-opts (if (.endsWith ^String filepath "cljc") {:read-cond :allow} {})]
              (if (= :unknown *read-eval*)
                (throw (IllegalStateException. "Unable to read source while *read-eval* is :unknown."))
                (read read-opts (PushbackReader. pbr)))
              (str text)))))))

(defn source-fn
  "A function for pulling in source code.

  Retrieves a specific subset of the source - in particular, returns a vector
  of source code (as strings) that includes the target line number and up to
  n preceding and following lines."
  [filepath line number]
  (when-let [strm (.getResourceAsStream (RT/baseLoader) filepath)]
    (with-open [rdr (LineNumberReader. (InputStreamReader. strm))]
      (println (- line number))
      (.setLineNumber rdr (- line number))
      (println (.getLineNumber rdr))
      (println (.readLine rdr))
      (dotimes [_ (inc (* number 2))] (.readLine rdr))
      (let [text (StringBuilder.)
            pbr (proxy [PushbackReader] [rdr]
                  (read [] (let [i (proxy-super read)]
                             (.append text (char i))
                             i)))
            read-opts (if (.endsWith ^String filepath "cljc") {:read-cont :allow} {})]
        (if (= :unknown *read-eval*)
          (throw (IllegalStateException. "Unable to read source while *read-eval* is :unknown."))
          (read read-opts (PushbackReader. pbr)))
        (str text)))))
