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

server(port) configs {
  fileRoot("src/test/resources")
}
```

###### context
```scala
server(port) configs {
  context("/hello")
}
```

#### Request matchers
##### URI matchers

match by URI
```scala
server(port) when {
  uri("/hello")
}
```

match URI by Regex
```scala
server(port) when {
  uri matched "/hello.+"
}
```

#### Multiple matchers

chain multiple matchers with same response
```scala
server(port) when {
  uri("/hello") and method("post")
} respond {
  text("world")
}
```

chain multiple matchers with different response

```scala
server(port) when {
  method("get")
} respond {
  text("get")
} when {
  method("post")
} respond {
  text("post")
}
```

##### Request method matcher
```scala
server(port) when {
  method("get")
}
```

##### Content matchers

match by exact body text
```scala
server(port) when {
  text("foo")
}
```

match body text by regex
```scala
server(port) when {
  text matched "hello.+"
}
```

match content using a file
```scala
server(port) when {
  file(getClass.getResource("/foo.request").getPath)
}
```

##### Query parameter matchers

match by exact value
```scala
server(port) when {
   query("foo") === "bar"
}
```

match value by regex
```scala
server(port) when {
   query("foo") matched ".+bar"
}
```

##### Header matchers

match by exact header value
```scala
server(port) when {
  header("Content-Type") === "application/json"
}
```

match header values by regex
```scala
server(port) when {
  header("Content-Type") matched ".+json"
}
```

##### HTTP version matcher
```scala
server(port) when {
  version("HTTP/1.0")
}
```

##### Cookie matchers

match by exact value
```scala
server(port) when {
  cookie("foo") === "bar"
}
```

match cookie value by regex:
```scala
server(port) when {
  cookie("foo") matched ".+bar"
}
```

##### Form matchers

match by exact form value
```scala
server(port) when {
  form("foo") === "bar"
}
````

match value by regex

```scala
server(port) when {
  form("foo") matched "ba.+"
}
````

##### XML body matchers
match by exact xml body
```scala
server(port) when {
  xml("<body>something</body>")
}
```

match content using a file
```scala
server(port) when {
  xml(file(getClass.getResource("/foo_request.xml").getPath))
}
```

###### Xpath matchers
match by exact xpath value
```scala
server(port) when {
  xpath("/request/parameters/id/text()") === "foo"
}

```

match xpath value by regex
```scala
server(port) when {
  xpath("/request/parameters/id/text()") matched "fo.+"
}

```

##### Json matchers
```scala
server(port) when {
  json("{\"foo\": \"bar\"}")
}
```

match content using a file
```scala
server(port) when {
  json(file(getClass.getResource("/foo_request.json").getPath))
}
```
###### Jsonpath matchers
match by exact json path value
```scala
server(port) when {
  jsonPath("$.foo") === "bar"
}
```

match json value by regex
```scala
server(port) when {
  jsonPath("$.foo") matched ".+bar"
}
```


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
server(port) when {
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
server(port) when {
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
server(port) on {
  complete{
    get("http://another_site")
  }
}
```


##### Asynchronous
You can use the async api to fire events asynchronsously

```scala
server(port) on {
  complete {
    async {
      get("http://another_site")
    }
  }
}
```

#### Multiple responses
```scala
server(port) when {
  uri("/not-exits")
} respond {
  status(400) and text("BAD REQUEST")
}
```

#### Multiple behaviours

```scala
server(port) when {
  method("get")
} respond {
  text("get")
} when {
  method("post")
} respond {
  text("post")
}
```
