package org.treppo.mocoscala.features

import java.net.URI

import org.apache.http.HttpVersion
import org.scalatest.{BeforeAndAfter, FunSpec}
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class RequestMatchersTest extends FunSpec with BeforeAndAfter with RemoteTestHelper {

  describe("request matchers") {

    describe("uri matchers") {

      they("match by uri") {
        val theServer = server when {
          uri("/hello")
        } respond {
          status(200) and text("world")
        }

        theServer running { uri: URI =>
          assert(getForStatus(uri.resolve("/hello")) === 200)
          assert(get(uri.resolve("/hello")) === "world")
        }
      }

      they("match uri by regex") {
        val theServer = server when {
          uri matched "/hello.+"
        } respond {
          text("world")
        }

        theServer running { url: URI =>
          assert(get(url.resolve("/hello123")) === "world")
          assert(get(url.resolve("/hello-abc")) === "world")
        }
      }
    }

    describe("multiple matchers") {
      they("chain multiple matchers with same response") {
        val theServer = server when {
          uri("/hello") and method("post")
        } respond {
          text("post")
        }

        theServer running { url: URI =>
          assert(post(url.resolve("/hello"), "") === "post")
        }
      }

      they("chain multiple matchers with different response") {
        val theServer = server when {
          method("get")
        } respond {
          text("get")
        } when {
          method("post")
        } respond {
          text("post")
        }

        theServer running { url: URI =>
          assert(get(url) === "get")
          assert(post(url) === "post")
        }
      }
    }

    describe("request method matcher") {
      it("matches by method") {
        val theServer = server when {
          method("get")
        } respond {
          text("get")
        }

        theServer running { url: URI =>
          assert(get(url) === "get")
        }
      }
    }

    describe("content matchers") {
      they("match by text") {
        val theServer = server when {
          text("hello")
        } respond {
          text("text matched")
        }

        theServer running { url: URI =>
          assert(post(url, "hello") === "text matched")
        }
      }

      they("match body text by regex") {
        val theServer = server when {
          text matched "hello.+"
        } respond {
          text("text matched")
        }

        theServer running { url: URI =>
          assert(post(url, "hello-abc") === "text matched")
          assert(post(url, "hello-123") === "text matched")
        }
      }

      they("match content using a file") {
        val theServer = server when {
          file(getClass.getResource("/foo_request.txt").getPath)
        } respond {
          text("text matched")
        }

        theServer running { url: URI =>
          assert(post(url, "a foo request") === "text matched")
        }
      }
    }

    describe("query parameter matchers") {
      they("match by exact value") {
        val theServer = server when {
          query("foo") === "bar"
        } respond {
          text("bar")
        }

        theServer running { url: URI =>
          assert(get(url.resolve("/hello?foo=bar")) === "bar")
        }
      }

      they("match value by regex") {
        val theServer = server when {
          query("foo") matched ".+bar"
        } respond {
          text("bar")
        }

        theServer running { url: URI =>
          assert(get(url.resolve("/hello?foo=123-bar")) === "bar")
          assert(get(url.resolve("/hello?foo=abc-bar")) === "bar")
        }
      }
    }

    describe("header matchers") {
      they("match by exact header value") {
        val theServer = server when {
          header("Content-Type") === "content-type"
        } respond {
          text("headers matched")
        }

        theServer running { uri: URI =>
          assert(getWithHeaders(uri, "Content-Type" -> "content-type") === "headers matched")
        }
      }

      they("match header values by regex") {
        val theServer = server when {
          header("Content-Type") matched ".+json"
        } respond {
          text("headers matched")
        }

        theServer running { uri: URI =>
          assert(getWithHeaders(uri, "Content-Type" -> "application/json") === "headers matched")
        }
      }
    }

    describe("HTTP version matcher") {
      it("matches by version") {
        val theServer = server when {
          version("HTTP/1.0")
        } respond {
          text("version matched")
        }

        theServer running { uri: URI =>
          assert(getWithVersion(uri, HttpVersion.HTTP_1_0) === "version matched")
        }
      }
    }

    describe("cookie matchers") {
      they("match by exact value") {
        val theServer = server when {
          cookie("foo") === "bar"
        } respond {
          status(200)
        }

        theServer running { uri: URI =>
          assert(getForStatusWithCookie(uri, ("foo", "bar")) === 200)
        }
      }

      they("match cookie value by regex") {
        val theServer = server when {
          cookie("foo") matched ".+bar"
        } respond {
          status(200)
        }

        theServer running { uri: URI =>
          assert(getForStatusWithCookie(uri, ("foo", "123-bar")) === 200)
          assert(getForStatusWithCookie(uri, ("foo", "abc-bar")) === 200)
        }
      }
    }

    describe("form matchers") {
      they("match by exact form value") {
        val theServer = server when {
          form("foo") === "bar"
        } respond {
          text("bar")
        }

        theServer running { uri: URI =>
          assert(postForm(uri, "foo" -> "bar") === "bar")
        }
      }

      they("match form value by regex") {
        val theServer = server when {
          form("foo") matched ".+bar"
        } respond {
          text("bar")
        }

        theServer running { uri: URI =>
          assert(postForm(uri, "foo" -> "123-bar") === "bar")
          assert(postForm(uri, "foo" -> "abc-bar") === "bar")
        }
      }
    }

    describe("xml body matchers") {
      they("match by exact xml body") {
        val theServer = server when {
          xml("<body>something</body>")
        } respond {
          status(200)
        }

        theServer running { uri: URI =>
          assert(postXmlForStatus(uri, "<body>something</body>") === 200)
        }
      }

      they("match content using a file") {
        val theServer = server when {
          xml(file(getClass.getResource("/foo_request.xml").getPath))
        } respond {
          text("text matched")
        }

        theServer running { uri: URI =>
          assert(post(uri, "<body>something</body>") === "text matched")
        }
      }

      describe("xpath matchers") {
        they("match by exact xpath value") {
          val theServer = server when {
            xpath("/body/text()") === "foo"
          } respond {
            status(200)
          }

          theServer running { uri: URI =>
            assert(postXmlForStatus(uri, "<body>foo</body>") === 200)
          }
        }

        they("match xpath value by regex") {
          val theServer = server when {
            xpath("/body/text()") matched ".+foo"
          } respond {
            status(200)
          }

          theServer running { uri: URI =>
            assert(postXmlForStatus(uri, "<body>123-foo</body>") === 200)
            assert(postXmlForStatus(uri, "<body>abc-foo</body>") === 200)
          }
        }
      }
    }

    describe("json body matchers") {
      they("match by exact json body") {
        val theServer = server when {
          json("""{"foo":"bar"}""")
        } respond {
          status(200)
        }

        theServer running { uri: URI =>
          assert(postJsonForStatus(uri, """{"foo":"bar"}""") === 200)
        }
      }

      they("match content using a file") {
        val theServer = server when {
          json(file(getClass.getResource("/foo_request.json").getPath))
        } respond {
          text("text matched")
        }

        theServer running { uri: URI =>
          assert(post(uri, """{"foo": "bar"}""") === "text matched")
        }
      }

      describe("jsonpath matchers") {
        they("match by exact json path value") {
          val theServer = server when {
            jsonPath("$.foo") === "bar"
          } respond {
            status(200)
          }

          theServer running { uri: URI =>
            assert(postJsonForStatus(uri, """{"foo":"bar"}""") === 200)
          }
        }

        they("match json value by regex") {
          val theServer = server when {
            jsonPath("$.foo") matched ".+bar"
          } respond {
            status(200)
          }

          theServer running { uri: URI =>
            assert(postJsonForStatus(uri, """{"foo":"123-bar"}""") === 200)
            assert(postJsonForStatus(uri, """{"foo":"abc-bar"}""") === 200)
          }
        }
      }
    }
  }
}
