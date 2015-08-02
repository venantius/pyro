(defproject venantius/pyro "0.1.0-SNAPSHOT"
  :description "Pyro: light up your stacktraces"
  :url "https://github.com/venantius/pyro"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :injections [(require 'pyro.printer) (pyro.printer/middleware)]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-stacktrace "0.2.8"]
                 [venantius/glow "0.1.0"]])
