(ns pyro.source
  "A namespace for reading source code off of the classpath"
  (:require [clojure.core.memoize :refer [lu]]
            [clojure.string :as string]
            [glow.colorschemes :as colorschemes]
            [glow.terminal :as terminal]
            [glow.parse :as parse]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [clojure.lang RT]
           [java.io BufferedReader InputStreamReader]
           (java.nio.charset StandardCharsets)))

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

(defn filepath->lastModified
  [filepath]
  (let [url (io/resource filepath)
        file (io/file filepath)]
    (cond
      ; handle files inside JARs
      ; (io/resource "clojure/java/io.clj") =>
      ; URL "jar:file:/Users/ivref/.m2/repository/org/clojure/clojure/1.8.0/clojure-1.8.0.jar!/clojure/java/io.clj"
      (and url (= "jar" (.getProtocol url))) (-> url
                                                 (.getFile)
                                                 (str/split #"!")
                                                 (first)
                                                 (subs 5) ; remove file:
                                                 (io/file)
                                                 (.lastModified))

      ; handle local files loaded using require and similar
      ; (io/resource "pyro/source.clj") =>
      ; URL "file:/Users/ivref/clojure/pyro/src/pyro/source.clj"
      (and url (= "file" (.getProtocol url))) (-> url (.getFile) (io/file) (.lastModified))

      ; handle files loaded using Cursive, load-file and similar
      (.exists file) (.lastModified file)

      :else (throw (ex-info "Could not find lastModified of filepath" {:filepath filepath})))))

(defn filepath->buffered-reader
  [filepath]
  (if-let [strm (or (.getResourceAsStream (RT/baseLoader) filepath)
                    (io/input-stream (io/file filepath)))]
    (BufferedReader. (InputStreamReader. strm StandardCharsets/UTF_8))
    (throw (ex-info "Could not get stream" {:filepath filepath}))))

(defn file-source
  [[filepath _]]
  (terminal/ansi-colorize
   colorschemes/terminal-default
   (parse/parse
    (string/join "\n" (line-seq (filepath->buffered-reader filepath))))))

(def memoized-file-source
  (lu file-source :lu/threshold 64))

(defn source-fn
  "A function for pulling in source code.

  Retrieves a specific subset of the source - in particular, returns a vector
  of source code (as strings) that includes the target line number and up to
  n preceding and following lines."
  {:added "0.1.0"}
  [filepath line number]
  (let [rdr (memoized-file-source [filepath (filepath->lastModified filepath)])]
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
