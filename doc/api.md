### Concept
There are three important concepts in this wrapper: __request matcher__, __response hander__, and __resource__.

__Request matcher:__ sits in the *when* clause, used to match against requests that server received, once request matched, moco server will respond.

__Response handler:__ sits in the *respond* clause, used to define which should be responded to client once request matched.

__Resource:__  Any thing which can be matched against or any thing which can be send back to client could be considered as a resource.


#### Config Apis:
Current moco support two global configurations: [file root](https://github.com/dreamhead/moco/blob/master/moco-doc/global-settings.md#file-root) and [context](https://github.com/dreamhead/moco/blob/master/moco-doc/global-settings.md#context).


###### file root

```scala
import org.treppo.mocoscala.dsl.Moco._

server(8080) configs {
  fileRoot("src/test/resources")
}
```

###### context
```scala
server(8080) configs {
  context("/hello")
}
```

#### Matcher Apis:
##### Uri

match by uri
```scala
server(8080) when {
  uri("/hello")
}
```
or match by regex
```scala
server(8080) when {
  uri matched "/hello.+"
}
```

##### Request method
```scala
server(8080) when {
  method("get")
}
```
##### Text

by value
```scala
server(8080) when {
  text("foo")
}
```
or by regex
```scala
server(8080) when {
  text matched "hello.+"
}
```

##### File
```scala
server(8080) when {
  file("foo.req")
}
```

##### Version
```scala
server(8080) when {
  version("HTTP/1.0")
}
```

##### Header

match by value
```scala
server(8080) when {
  header("Content-Type") === "application/json"
}
```
or by regex
```scala
server(8080) when {
  header("Content-Type") matched ".+json"
}
```
##### Query

match by value
```scala
server(8080) when {
   query("foo") === "bar"
}
```
or by regex:
```scala
server(8080) when {
   query("foo") matched ".+bar"
}
```

##### Cookie

match by value
```scala
server(8080) when {
  cookie("foo") === "bar"
}
```
  or by regex:
```scala
server(8080) when {
  cookie("foo") matched ".+bar"
}
```

##### Form

you can do exact match by form value
```scala
server(8080) when {
  form("foo") === "bar"
}
````
or by match value with regex

```scala
server(8080) when {
  form("foo") matched "ba.+"
}
````

##### Xml

```scala
server(8080) when {
  xml("<body>something</body>")
}
```

##### Xpath
similarly, you can do exact match by value
```scala
server(8080) when {
  xpath("/request/parameters/id/text()") === "foo"
}

```
or match by regex

```scala
server(8080) when {
  xpath("/request/parameters/id/text()") matched "fo.+"
}

```

##### Json
```scala
server(8080) when {
  json("{\"foo\": \"bar\"}")
}
```
##### Jsonpath
similar to xpath.

#### Response Apis:

##### Text

```scala
respond {
  text("foo")
}
```

##### File
```scala
respond {
  file("foo.req")
}
```

##### Header

```scala
respond {
  headers("Content-Type" -> "json", "Accept" -> "html")
}
```

##### Cookie

```scala
respond {
  cookie("foo" -> "bar")
}
```

##### Status

```scala
respond {
  status 200
}
```
##### Version

```scala
respond {
  version("HTTP/1.0")
}
```

##### Proxy Apis


##### Single URL
Respond with a specified url, just like a proxy.

```scala
respond {
  proxy("http://example.com")
}
```
##### Failover
Proxy also supports failover

```scala
respond {
  proxy("http://example.com") {
    failover("failover.json")
  }
}
```

##### Playback
Supports playback saving remote request and response into local file.

```scala
respond {
  proxy("http://example.com") {
    playback("playback.json")
  }
}
```

##### Batch URLs
Proxy also support proxying a batch of URLs in the same context

```scala
server(8080) when {
  method("GET") and uri matched "/proxy/.*"
} respond {
  proxy {
    from("/proxy") to "http://localhost:9090/target"
  }
}
```

##### Redirect Api:
You can simply redirect a request to a different location:

```scala
server(8080) when {
  uri("/redirect")
} respond {
  redirectTo("/target")
}
```

##### Attachment
You can setup an attachment as response
```scala
respond {
  attachment("filename", file("filepath"))
}
```

##### Latency
You can simulate a slow response:

```scala
import scala.concurrent.duration.DurationInt

respond {
  latency(2.seconds)
}
```

##### Sequence
You can simulate a sequence of responses:

```scala
respond {
  seq("foo", "bar", "blah")
}
```


##### Event
You can specify a subsequent action once the response was sent:

```scala
server(8080) on {
  complete{
    get("http://another_site")
  }
}
```


##### Asynchronous
You can use the async api to fire events asynchronsously

```scala
server(8080) on {
  complete {
    async {
      get("http://another_site")
    }
  }
}
```

#### Multiple matchers

```scala
server(8080) when {
  uri("/hello") and method("post")
} respond {
  text("world")
}
```
#### Multiple responses
```scala
server(8080) when {
  uri("/not-exits")
} respond {
  status(400) and text("BAD REQUEST")
}
```

#### Multiple behaviours

```scala
server(8080) when {
  method("get")
} respond {
  text("get")
} when {
  method("post")
} respond {
  text("post")
}
```
