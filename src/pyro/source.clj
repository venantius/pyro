(ns pyro.source
  "A namespace for reading source code off of the classpath"
  (:import [java.io BufferedReader InputStreamReader]
           [clojure.lang RT]))

(defn- pad
  [s]
  (str "    " s))

(defn- pad-arrow
  [s]
  (str "--> " s))

(defn source-fn
  "A function for pulling in source code.

  Retrieves a specific subset of the source - in particular, returns a vector
  of source code (as strings) that includes the target line number and up to
  n preceding and following lines."
  [filepath line number]
  (when-let [strm (.getResourceAsStream (RT/baseLoader) filepath)]
    (with-open [rdr (BufferedReader. (InputStreamReader. strm))]
      (let [rdr (drop (- line (inc number)) (line-seq rdr))]
        (let [pre (take number rdr)
              line (nth rdr number)
              post (drop (inc number) (take (inc (* number 2)) rdr))]
          (clojure.string/join "\n" (flatten
                                     [(map pad pre)
                                      (pad-arrow line)
                                      (map pad post)])))))))
