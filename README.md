# Pyro

Pyro is a library for getting exactly what you need out of Clojure stacktraces. It can be thought of as a "booster pack" for clj-stacktrace, with easy configuration for your project's needs.

![example](http://venantius.github.io/pyro/doc/screenshot.png)

## A Cautionary Note

Pyro is currently alpha software. It may break in unforseen ways and is not guaranteed to be particularly performant. It is advised that Pyro be used in
development environments ONLY, and not in production environments.

For the curious, the big thing that slows down speed is having to look up source code and syntax-highlight it. At the moment Pyro ships with a LU cache to help with cases where the same files cause repeated stacktraces. This comes with some memory overhead but ensures that if your program starts blowing up with stacktraces that all traces after the first will be delivered quickly

## Installation

To use Pyro in your project, just add the following to the :dependencies key of your project.clj:

```clojure
[venantius/pyro "0.1.1"]
```

## Usage

The key method in Pyro is `pyro.printer/pprint-exception`. However, you're unlikely to want to manually trigger it - instead, you'll probably want to use it in place of clojure's default stacktrace invocation methods.

You can sub Pyro's exception printer in for Clojure's default stacktrace invocation methods as follows:

```clojure
(require '[pyro.printer :as printer])
(printer/swap-stacktrace-engine!)
```

### Configuration

`swap-stacktrace-engine!` can be provided with an options map with the following options:

```clojure
{:show-source true
 :drop-nrepl-elements true
 :hide-clojure-elements true
 :hide-lein-elements true
 :ns-whitelist nil}
```

##### `:show-source`

Boolean. If true, prints syntax-highlighted source code as part of the stacktrace.

##### `:drop-nrepl-elements`

Boolean. If true, drops all stackframe elements until a `clojure.main/repl/read-eval-print` frame.

##### `:hide-clojure-elements`

Boolean. If true, removes all stacktrace frames belonging to `clojure.core`, `clojure.lang`, etc.

##### `:hide-lein-elements`

Boolean. If true, removes all stacktrace frames belonging to `leiningen.core.eval`, `leiningen.test`, and `leiningen.core.main`.

##### `:ns-whitelist`

A regex literal. If present, will filter for only those stacktrace frames where the namespace matches one or more of the regular expressions.

## Special Thanks

I owe thanks to both the folks at AvisoNovate for inspiring me, and to all of the developers on [clj-stacktrace](https://github.com/mmcgrana/clj-stacktrace), for providing an excellent scaffolding on which to build.

## License

Copyright © 2017 W. David Jarvis

Distributed under the Eclipse Public License, the same as Clojure.
