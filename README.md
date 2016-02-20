moco-scala
==========

This is a scala wrapper for [moco](https://github.com/dreamhead/moco).
The purpose of this project is to leverage scala's elegant syntax to provide beautiful DSL for using moco in scala testing.

[![Build Status](https://travis-ci.org/nicholasren/moco-scala.svg?branch=master)](https://travis-ci.org/nicholasren/moco-scala)

### How to use

#### Add dependency
```sbt
libraryDependencies += "org.treppo" %% "moco-scala" % "0.3"
```

#### Try latest version
If you want to try latest version, just add snapshot repo to dependency resolver

```sbt
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
```

### Quick Start

#### Import dependencies
```scala
import org.treppo.moco.dsl.SMoco._
```

#### Create server
```scala
val theServer = server(8080)
```

#### Record behaviour
```scala
theServer when { uri("/hello") } then { status(200) }
```

#### Running server and test your stuff
```scala
theServer running  {
    assert(getForStatus(remoteUrl("/hello")) === 200)
}
```

### Document
Api documentation can be found [here.](doc/api.md)

Also, please refer to [functional tests](https://github.com/nicholasren/moco-scala/tree/master/src/test/scala/features) for detail usage.

### Contribution:
This project is still in process, any question or suggestion is more than welcome.
