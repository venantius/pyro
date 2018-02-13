(ns pyro.source
  "A namespace for reading source code off of the classpath"
  (:require [clojure.core.memoize :refer [lu]]
            [clojure.string :as string]
            [glow.ansi :as ansi]
            [glow.core :as glow]
            [glow.parse :as parse]
            [instaparse.core :as insta])
  (:import [clojure.lang RT]
           [java.io BufferedReader InputStreamReader]))

(defn pad-integer
  "Right-pad an integer with spaces until it takes up 4 character spaces."
  [n]
  (let [len (-> n str count)]
    (str n (apply str (repeat (- 4 len) " ")))))

(defn pad-source
  [s n]
  (str "    " (pad-integer n) " " s))

(defn pad-source-arrow
  [s n]
  (str "--> " (pad-integer n) " " s))

(defn filepath->buffered-reader
  [filepath]
  (when-let [strm (.getResourceAsStream (RT/baseLoader) filepath)]
    (BufferedReader. (InputStreamReader. strm))))

(defn file-source
  [filepath]
  (glow/highlight
   (string/join "\n" (line-seq (filepath->buffered-reader filepath)))))

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
                        (string/split rdr #"\n"))
          pre (take number content)
          line-code (nth content number)
          post (drop (inc number)
                     (take (inc (* number 2)) content))]
      (string/join "\n" (flatten
                         [(map pad-source pre (range (- line number) line))
                          (pad-source-arrow line-code line)
                          (map pad-source post (range (inc line) (inc (+ line number))))])))))

(defn ns->filename
  "Given a namespace string, convert it to a filename."
  [n f]
  (let [n (-> n
              (string/replace "-" "_")
              (string/split #"\.")
              drop-last
              vec
              (conj f))]
    (string/join "/" n)))

(defn get-var-filename
  "Given a var or class, return the filename in question."
  {:added "0.1.0"}
  [{:keys [ns fn file] :as element}]
  (if (= fn "fn")
    ;; anonymous fn, but file exists
    (ns->filename ns file)
    ;; defined var
    (-> (symbol ns fn) resolve meta :file)))
