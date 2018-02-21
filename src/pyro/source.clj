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
           [java.io BufferedReader InputStreamReader File]
           [java.nio.charset StandardCharsets]
           [java.net URLDecoder]))

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

(defn file-exists-or-nil [^String file]
  (when (and file (.exists (io/file file)))
    (io/file file)))

(defn resource->containing-file
  "This function returns the containing file of a resource.

  This can be a JAR file for a clj resource inside a dependency,
  or a regular clj file for a clj resource used during development."
  [filepath]
  (when-let [url (io/resource filepath)]
    (let [url-str (str url)]
      (cond (str/starts-with? url-str (str "jar:file:" File/separator))
            ; This handles clj files inside JARs, such as
            ; (resource->file "clojure/java/io.clj")
            ; => #object[java.io.File 0x74900942 "/home/ire/.m2/repository/org/clojure/clojure/1.8.0/clojure-1.8.0.jar"]
            (-> url-str
                (subs (count "jar:file:"))
                (URLDecoder/decode "UTF-8")
                (str/split #"!")
                (drop-last)
                ((fn [x] (str/join "!" x)))
                (file-exists-or-nil))

            ; This handles standalone clj files present during development, such as
            ; (io/resource "pyro/source.clj")
            ; => #object[java.net.URL 0x2ab4619e "file:/home/ire/code/github/pyro/src/pyro/source.clj"]
            (str/starts-with? url-str (str "file:" File/separator))
            (-> url-str
                (subs (count "file:"))
                (URLDecoder/decode "UTF-8")
                (file-exists-or-nil))))))

(defn filepath->lastModified
  [filepath]
  (when-let [file (or (resource->containing-file filepath)
                      (file-exists-or-nil filepath))]
    (.lastModified file)))

(defn filepath->buffered-reader
  [filepath]
  (when-let [strm (or (.getResourceAsStream (RT/baseLoader) filepath)
                    (io/input-stream (io/file filepath)))]
    (BufferedReader. (InputStreamReader. strm StandardCharsets/UTF_8))))

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
  both the filepath and the lastModified of the file.
  This is the function that is actually memoized, and thus
  the cache will never return old or expired files."
  (let [invoke-file-source (fn [filepath _] (file-source filepath))
        memoized-file-source (lu invoke-file-source :lu/threshold 64)]
    (fn [filepath]
      (memoized-file-source filepath (filepath->lastModified filepath)))))

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
