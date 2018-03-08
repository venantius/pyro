(defproject venantius/pyro "0.1.2"
  :description "Pyro: light up your stacktraces"
  :url "https://github.com/venantius/pyro"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :test-selectors {:default (complement :demo)
                   :demo :demo}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.memoize "0.5.6"]
                 [clj-stacktrace "0.2.8"]
                 [venantius/glow "0.1.5" :exclusions [hiccup garden]]])
