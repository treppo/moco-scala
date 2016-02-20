package features

import org.apache.http.HttpVersion
import org.scalatest.{BeforeAndAfter, FunSpec}
import org.treppo.mocoscala.dsl.Conversions._
import org.treppo.mocoscala.dsl.SMoco
import org.treppo.mocoscala.dsl.SMoco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class RequestMatcherTest extends FunSpec with BeforeAndAfter with RemoteTestHelper {

  val port = 8081

  var theServer: SMoco = null

  before {
    theServer = server(port)
  }

  describe("matchers") {

    it("match by uri") {
      theServer when {
        uri("/hello") and method("post")
      } then {
        status(200) and text("world")
      }


      theServer running {
        assert(getForStatus(remoteUrl("/hello")) === 400)
        assert(post(remoteUrl("/hello"), "") === "world")
      }
    }

    it("match uri by regex") {
      theServer when {
        uri matched "/hello.+"
      } then {
        text("world")
      }


      theServer running {
        assert(get(remoteUrl("/hello123")) === "world")
        assert(get(remoteUrl("/hello-abc")) === "world")
      }
    }

    it("match by query parameters") {
      theServer when {
        query("foo") === "bar"
      } then {
        text("bar")
      }

      theServer running {
        assert(get(remoteUrl("/hello?foo=bar")) === "bar")
      }

    }

    it("match header by regex") {
      theServer when {
        header("Content-Type") matched ".+json"
      } then {
        text("headers matched")
      }


      theServer running {
        assert(getWithHeaders("Content-Type" -> "application/json") === "headers matched")
        assert(getForStatusWithHeaders("Content-Type" -> "html/text") === 400)
      }
    }

    it("match text by regex") {
      theServer when {
        text matched "hello.+"
      } then {
        text("text matched")
      }

      theServer running {
        assert(post(root, "hello-abc") === "text matched")
        assert(post(root, "hello-123") === "text matched")
      }

    }

    it("match by method") {
      theServer when {
        method("get")
      } then {
        text("get")
      }


      theServer running {
        assert(get(root) === "get")
      }
    }

    it("match by headers") {
      theServer when {
        header("Content-Type") === "content-type"
      } then {
        text("headers matched")
      }

      theServer running {
        assert(getWithHeaders("Content-Type" -> "content-type") === "headers matched")
      }
    }

    it("match by version") {
      theServer when {
        version("HTTP/1.0")
      } then {
        text("version matched")
      }

      theServer running {
        assert(getWithVersion(root, HttpVersion.HTTP_1_0) === "version matched")
      }
    }

    it("match by cookie") {
      theServer default {
        cookies("foo" -> "bar") and status(302)
      } when {
        cookie("foo") === "bar"
      } then {
        status(200)
      }

      theServer running {
        assert(getForStatus(root) === 302)
        assert(getForStatus(root) === 200)
      }
    }

    it("match by form") {
      theServer when {
        form("foo") === "bar"
      } then {
        text("bar")
      }

      theServer running {
        assert(post(root, "foo" -> "bar") === "bar")
      }

    }

    it("can define multi matchers") {
      theServer when {
        method("get")
      } then {
        text("get")
      } when {
        method("post")
      } then {
        text("post")
      }

      theServer running {
        assert(get(root) === "get")
        assert(post(root, "") === "post")
      }
    }
  }
}
