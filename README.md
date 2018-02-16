# Pyro

Pyro is a library for getting exactly what you need out of Clojure stacktraces. It can be thought of as a "booster pack" for clj-stacktrace, with easy configuration for your project's needs.

![example](http://venantius.github.io/pyro/doc/screenshot.png)

## A Cautionary Note

Pyro is currently alpha software. It may break in unforseen ways and is not guaranteed to be particularly performant. It is advised that Pyro be used in
development environments ONLY, and not in production environments.

For the curious, the big thing that slows down speed is having to look up source code and syntax-highlight it. At the moment Pyro ships with a LU cache to help with cases where the same files cause repeated stacktraces. This comes with some memory overhead but ensures that if your program starts blowing up with stacktraces that all traces after the first will be delivered quickly

## Usage

The key method in Pyro is `pyro.printer/pprint-exception`. However, you're unlikely to want to manually trigger it - instead, you'll probably want to use it in place of clojure's default stacktrace invocation methods.

You can sub Pyro's exception printer in for Clojure's default stacktrace invocation methods as follows:

```clojure
(require '[pyro.printer :as printer])
(printer/swap-stacktrace-engine!)
```

`swap-stacktrace-engine!` can also be provided with an options map with the following options:

 * `:show-source`
 * `:drop-nrepl-elements`
 * `:hide-clojure-elements`
 * `:hide-lein-elements`

By default, all of these options are set to true.

## Special Thanks

I owe thanks to both the folks at AvisoNovate for inspiring me, and to all of the developers on [clj-stacktrace](https://github.com/mmcgrana/clj-stacktrace), for providing an excellent scaffolding on which to build.

## License

Copyright © 2017 W. David Jarvis

Distributed under the Eclipse Public License, the same as Clojure.
