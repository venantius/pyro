(ns pyro.dummy-fns
  "Functions outside of the testing namespace that throw stacktraces.")

(defn i-dont-work
  []
  (+ "a" 2))
