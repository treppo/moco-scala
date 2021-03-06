moco-scala
==========
[![Build Status](https://travis-ci.org/treppo/moco-scala.svg?branch=master)](https://travis-ci.org/treppo/moco-scala)


This is a scala wrapper for [moco](https://github.com/dreamhead/moco), based on the unmaintained
[moco-scala](https://github.com/nicholasren/moco-scala) by Nicholas Ren.
In contrast to the the previous versions by Nicholas, the Moco server does not have mutable state.

## How to use
### Add dependency
```sbt
libraryDependencies += "org.treppo" %% "moco-scala" % "0.5.2" % Test
```

### Quick Start
```scala
// Import dependencies
import org.treppo.mocoscala.dsl.Moco._

// Create server with port
val theServer = server(8080) when { uri("/hello") } respond { status(200) }

// Create server with random port
val theServer = server when { uri("/hello") } respond { status(200) }

// Running server and test your stuff
theServer running  { uri: URI =>
  assert(SomeHttpClient().url(uri).get.status === 200)
}
```

## Documentation
Detailed feature documentation can be found in the [doc/api.md](doc/api.md)

Also, please refer to [functional tests](https://github.com/treppo/moco-scala/tree/master/src/test/scala/org.treppo.mocoscala.features).

## Contribution:
Questions, suggestions or pull requests are more than welcome in the Issues section.

## License
Licensed under the [MIT license](https://github.com/treppo/moco-scala/blob/master/MIT-LICENSE.txt)
