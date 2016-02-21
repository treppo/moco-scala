package features

import org.apache.http.HttpVersion
import org.scalatest.{BeforeAndAfter, FunSpec}
import org.treppo.mocoscala.dsl.Conversions._
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class RequestMatcherTest extends FunSpec with BeforeAndAfter with RemoteTestHelper {

  val port = 8081

  describe("matchers") {


    it("match by uri") {
      val theServer = server(port) when {
        uri("/hello") and method("post")
      } respond {
        status(200) and text("world")
      }

      theServer running {
        assert(getForStatus("/hello") === 400)
        assert(post("/hello", "") === "world")
      }
    }

    it("match uri by regex") {
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

    it("match by query parameters") {
      val theServer = server(port) when {
        query("foo") === "bar"
      } respond {
        text("bar")
      }

      theServer running {
        assert(get("/hello?foo=bar") === "bar")
      }

    }

    it("match header by regex") {
      val theServer = server(port) when {
        header("Content-Type") matched ".+json"
      } respond {
        text("headers matched")
      }

      theServer running {
        assert(getWithHeaders("Content-Type" -> "application/json") === "headers matched")
        assert(getForStatusWithHeaders("Content-Type" -> "html/text") === 400)
      }
    }

    it("match text by regex") {
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

    it("match by method") {
      val theServer = server(port) when {
        method("get")
      } respond {
        text("get")
      }

      theServer running {
        assert(get === "get")
      }
    }

    it("match by headers") {
      val theServer = server(port) when {
        header("Content-Type") === "content-type"
      } respond {
        text("headers matched")
      }

      theServer running {
        assert(getWithHeaders("Content-Type" -> "content-type") === "headers matched")
      }
    }

    it("match by version") {
      val theServer = server(port) when {
        version("HTTP/1.0")
      } respond {
        text("version matched")
      }

      theServer running {
        assert(getWithVersion(HttpVersion.HTTP_1_0) === "version matched")
      }
    }

    it("match by cookie") {
      val theServer = server(port) when {
        cookie("foo") === "bar"
      } respond {
        status(400)
      }

      theServer running {
        assert(getForStatusWithCookie(("foo", "bar")) === 400)
      }
    }

    it("match by form") {
      val theServer = server(port) when {
        form("foo") === "bar"
      } respond {
        text("bar")
      }

      theServer running {
        assert(postForm("foo" -> "bar") === "bar")
      }
    }

    it("can define multi matchers") {
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
}
