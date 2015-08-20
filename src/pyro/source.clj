(ns pyro.source
  "A namespace for reading source code off of the classpath"
  (:require [clojure.core.memoize :refer [lu]]
            [glow.ansi :as ansi]
            [glow.core :as glow]
            [glow.parse :as parse]
            [instaparse.core :as insta])
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

(defn file-source
  [filepath]
  (when-let [strm (.getResourceAsStream (RT/baseLoader) filepath)]
    (let [rdr (BufferedReader. (InputStreamReader. strm))]
      (glow/highlight
       (clojure.string/join "\n" (line-seq rdr))))))

(def memoized-file-source
  (lu file-source :lu/threshold 64))

(defn source-fn
  "A function for pulling in source code.

  Retrieves a specific subset of the source - in particular, returns a vector
  of source code (as strings) that includes the target line number and up to
  n preceding and following lines."
  {:added "0.1.0"}
  [filepath line number]
  (let [rdr (memoized-file-source filepath)]
    (let [content (drop (- line (inc number))
                        (clojure.string/split rdr #"\n"))
          pre (take number content)
          line-code (nth content number)
          post (drop (inc number)
                     (take (inc (* number 2)) content))]
      (clojure.string/join "\n" (flatten
                                 [(map pad-source pre (range (- line number) line))
                                  (pad-source-arrow line-code line)
                                  (map pad-source post (range (inc line) (inc (+ line number))))])))))

(defn get-var-filename
  "Given a var or class, return the filename in question."
  {:added "0.1.0"}
  [n s]
  (-> (symbol n s) resolve meta :file))
