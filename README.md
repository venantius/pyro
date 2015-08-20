# pyro

Pyro is a library for getting exactly what you need out of Clojure stacktraces. It can be thought of as a "booster pack" for clj-stacktrace, with easy configuration for your project's needs.

## TODO

"Fast" (only useable for interned vars, not anonymous functions)
"Slow" (provides overall file context)
Both rely on memoization with a relatively low TTL; this comes with some memory overhead but ensures that if your program starts blowing up with stacktraces that all traces after the first will be delivered quickly

## Usage

FIXME

## Contributing

FIXME

## Special Thanks

I owe thanks to both the folks at AvisoNovate for inspiring me, and to all of the developers on [clj-stacktrace](https://github.com/mmcgrana/clj-stacktrace), for providing an excellent scaffolding on which to build.

## License

Copyright © 2015 W. David Jarvis

Distributed under the Eclipse Public License, the same as Clojure. 
