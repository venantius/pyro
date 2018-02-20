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

(defn resource->file
  [filepath]
  (when-let [url (io/resource filepath)]
    (let [url-str (str url)]
      (cond (str/starts-with? url-str (str "jar:file:" File/separator))
            (-> url-str
                (subs (count "jar:file:"))
                (URLDecoder/decode "UTF-8")
                (str/split #"!")
                (drop-last)
                ((fn [x] (str/join "!" x)))
                (file-exists-or-nil))

            (str/starts-with? url-str (str "file:" File/separator))
            (-> url-str
                (subs (count "file:"))
                (URLDecoder/decode "UTF-8")
                (file-exists-or-nil))))))

(defn filepath->lastModified
  [filepath]
  (when-let [file (or (resource->file filepath)
                      (file-exists-or-nil filepath))]
    (.lastModified file)))

(defn filepath->buffered-reader
  [filepath]
  (when-let [strm (or (.getResourceAsStream (RT/baseLoader) filepath)
                    (io/input-stream (io/file filepath)))]
    (BufferedReader. (InputStreamReader. strm StandardCharsets/UTF_8))))

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
