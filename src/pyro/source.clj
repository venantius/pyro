(ns pyro.source
  "A namespace for reading source code off of the classpath"
  (:require [glow.ansi :as ansi]
            [glow.core :as glow])
  (:import [clojure.lang RT]
           [java.io BufferedReader InputStreamReader]))

(defn pad-integer
  [n]
  (let [len (-> n str count)]
    (str n (apply str (repeat (- 4 len) " ")))))

(defn pad-source
  [s n]
  (str "    " (pad-integer n) " " s))

(defn pad-source-arrow
  [s n]
  (str "--> " (pad-integer n) " " s))

(defn source-fn
  "A function for pulling in source code.

  Retrieves a specific subset of the source - in particular, returns a vector
  of source code (as strings) that includes the target line number and up to
  n preceding and following lines."
  {:added "0.1.0"}
  [filepath line number]
  (when-let [strm (.getResourceAsStream (RT/baseLoader) filepath)]
    (with-open [rdr (BufferedReader. (InputStreamReader. strm))]
      (let [rdr (drop (- line (inc number)) (line-seq rdr))]
        (let [pre (map glow/highlight (take number rdr))
              line-code (glow/highlight (nth rdr number))
              post (map glow/highlight
                        (drop (inc number)
                              (take (inc (* number 2)) rdr)))]
          (clojure.string/join "\n" (flatten
                                     [(map pad-source pre (range (- line number) line))
                                      (pad-source-arrow line-code line)
                                      (map pad-source post (range (inc line) (inc (+ line number))))])))))))

(defn get-var-filename
  "Given a var or class, return the filename in question."
  {:added "0.1.0"}
  [v]
  (-> v symbol resolve meta :file))
