(ns pyro.source
  "A namespace for reading source code off of the classpath"
  (:require [clojure.core.memoize :refer [lu]]
            [clojure.string :as string]
            [glow.colorschemes :as colorschemes]
            [glow.terminal :as terminal]
            [glow.parse :as parse]
            [clojure.java.io :as io])
  (:import [clojure.lang RT]
           [java.io BufferedReader InputStreamReader BufferedInputStream]
           [java.nio.charset StandardCharsets]
           [java.security MessageDigest DigestInputStream]
           [javax.xml.bind DatatypeConverter]))

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

(defn filepath->stream
  "Returns a buffered stream of filepath, either using classpath or file from disk.
  Returns nil if filepath cannot be found."
  [filepath]
  (some-> (or (.getResourceAsStream (RT/baseLoader) filepath)
              (let [file (io/file filepath)]
                (when (.exists file)
                  (io/input-stream file))))
          (BufferedInputStream.)))

(defn filepath->buffered-reader
  [filepath]
  (some-> (filepath->stream filepath)
          (InputStreamReader. StandardCharsets/UTF_8)
          (BufferedReader.)))

(defn filepath->md5
  "Returns md5 of filepath, or nil if filepath cannot be found."
  [filepath]
  ; adopted from https://clojuredocs.org/clojure.core/while#example-579c5feae4b0bafd3e2a04c3
  (let [sha (MessageDigest/getInstance "MD5")
        buf (byte-array 8192)]
    (when-let [stream (filepath->stream filepath)]
      (with-open [dis (DigestInputStream. stream sha)]
        (while (> (.read dis buf 0 8192) -1)))
      (DatatypeConverter/printHexBinary (.digest sha)))))

(defn file-source
  "A function for getting colorized source of filepath."
  [filepath]
  (terminal/ansi-colorize
   colorschemes/terminal-default
   (parse/parse
    (string/join "\n" (line-seq (filepath->buffered-reader filepath))))))

(def memoized-file-source
  "Returns a memoized file-source function that detects changes to files.

  This function invokes an inner function taking two arguments,
  both the filepath and md5 sum of the file.
  This is the function that is actually memoized, and thus
  the cache will not return old or expired files."
  (let [cached-fn (lu (fn [filepath _] ; ignore md5 argument, only used by memoize/lu
                        (file-source filepath))
                      :lu/threshold 64)]
    (fn [filepath]
      (cached-fn filepath (filepath->md5 filepath)))))

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
