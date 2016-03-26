package features

import org.apache.http.HttpVersion
import org.scalatest.{BeforeAndAfter, FunSpec}
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class RequestMatchersTest extends FunSpec with BeforeAndAfter with RemoteTestHelper {

  val port = 8081

  describe("request matchers") {

    describe("uri matchers") {

      they("match by uri") {
        val theServer = server(port) when {
          uri("/hello")
        } respond {
          status(200) and text("world")
        }

        theServer running {
          assert(getForStatus("/hello") === 200)
          assert(post("/hello", "") === "world")
        }
      }

      they("match uri by regex") {
        val theServer = server(port) when {
          uri matched "/hello.+"
        } respond {
          text("world")
        }

        theServer running {
          assert(get("/hello123") === "world")
          assert(get("/hello-abc") === "world")
        }
      }
    }

    describe("multiple matchers") {
      they("chain multiple matchers with same response") {
        val theServer = server(port) when {
          uri("/hello") and method("post")
        } respond {
          text("post")
        }

        theServer running {
          assert(post("/hello", "") === "post")
        }
      }

      they("chain multiple matchers with different response") {
        val theServer = server(port) when {
          method("get")
        } respond {
          text("get")
        } when {
          method("post")
        } respond {
          text("post")
        }

        theServer running {
          assert(get === "get")
          assert(post("") === "post")
        }
      }
    }

    describe("request method matcher") {
      it("matches by method") {
        val theServer = server(port) when {
          method("get")
        } respond {
          text("get")
        }

        theServer running {
          assert(get === "get")
        }
      }
    }

    describe("body text matchers") {
      they("match by text") {
        val theServer = server(port) when {
          text("hello")
        } respond {
          text("text matched")
        }

        theServer running {
          assert(post("hello") === "text matched")
        }
      }

      they("match body text by regex") {
        val theServer = server(port) when {
          text matched "hello.+"
        } respond {
          text("text matched")
        }

        theServer running {
          assert(post("hello-abc") === "text matched")
          assert(post("hello-123") === "text matched")
        }
      }
    }

    describe("query parameter matchers") {
      they("match by exact value") {
        val theServer = server(port) when {
          query("foo") === "bar"
        } respond {
          text("bar")
        }

        theServer running {
          assert(get("/hello?foo=bar") === "bar")
        }
      }

      they("match value by regex") {
        val theServer = server(port) when {
          query("foo") matched ".+bar"
        } respond {
          text("bar")
        }

        theServer running {
          assert(get("/hello?foo=123-bar") === "bar")
          assert(get("/hello?foo=abc-bar") === "bar")
        }
      }
    }

    describe("header matchers") {
      they("match by exact header value") {
        val theServer = server(port) when {
          header("Content-Type") === "content-type"
        } respond {
          text("headers matched")
        }

        theServer running {
          assert(getWithHeaders("Content-Type" -> "content-type") === "headers matched")
        }
      }

      they("match header values by regex") {
        val theServer = server(port) when {
          header("Content-Type") matched ".+json"
        } respond {
          text("headers matched")
        }

        theServer running {
          assert(getWithHeaders("Content-Type" -> "application/json") === "headers matched")
        }
      }
    }

    describe("HTTP version matcher") {
      it("matches by version") {
        val theServer = server(port) when {
          version("HTTP/1.0")
        } respond {
          text("version matched")
        }

        theServer running {
          assert(getWithVersion(HttpVersion.HTTP_1_0) === "version matched")
        }
      }
    }

    describe("cookie matchers") {
      they("match by exact value") {
        val theServer = server(port) when {
          cookie("foo") === "bar"
        } respond {
          status(200)
        }

        theServer running {
          assert(getForStatusWithCookie(("foo", "bar")) === 200)
        }
      }

      they("match cookie value by regex") {
        val theServer = server(port) when {
          cookie("foo") matched ".+bar"
        } respond {
          status(200)
        }

        theServer running {
          assert(getForStatusWithCookie(("foo", "123-bar")) === 200)
          assert(getForStatusWithCookie(("foo", "abc-bar")) === 200)
        }
      }
    }

    describe("form matchers") {
      they("match by exact form value") {
        val theServer = server(port) when {
          form("foo") === "bar"
        } respond {
          text("bar")
        }

        theServer running {
          assert(postForm("foo" -> "bar") === "bar")
        }
      }

      they("match form value by regex") {
        val theServer = server(port) when {
          form("foo") matched ".+bar"
        } respond {
          text("bar")
        }

        theServer running {
          assert(postForm("foo" -> "123-bar") === "bar")
          assert(postForm("foo" -> "abc-bar") === "bar")
        }
      }
    }
  }
}
