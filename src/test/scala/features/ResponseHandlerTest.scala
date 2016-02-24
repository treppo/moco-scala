package features

import java.util.concurrent.TimeUnit

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

import scala.concurrent.duration.Duration

class ResponseHandlerTest extends FunSpec with BeforeAndAfter with RemoteTestHelper {

  override val port = 8080

  describe("default") {
    it("send default response") {
      val theServer = server(port) respond {
        text("default")
      }

      theServer running {
        assert(get === "default")
      }
    }
  }

  describe("redirect") {
    it("redirect to expected url") {
      val theServer = server(port) when {
        uri("/")
      } respond {
        text("foo")
      } when {
        uri("/redirect")
      } respond {
        redirectTo("/")
      }

      theServer running {
        assert(get("/redirect") === "foo")
      }
    }
  }

  describe("latency") {
    it("wait for a while") {
      val duration = Duration(1, TimeUnit.SECONDS)

      val theServer = server(port) respond {
        latency(duration)
      }

      theServer running {
        val start = System.currentTimeMillis()
        getForStatus
        val stop = System.currentTimeMillis()

        assert((stop - start) >= duration.toMillis)
      }
    }
  }

  describe("responses") {
    it("send text") {
      val theServer = server(port) when {
        method("get")
      } respond {
        text("get")
      }

      theServer running {
        assert(get === "get")
      }
    }

    it("send headers") {
      val theServer = server(port) when {
        method("get")
      } respond {
        headers("Content-Type" -> "json", "Accept" -> "html")
      }

      theServer running {
        assert(getForHeader("Content-Type") === "json")
        assert(getForHeader("Accept") === "html")
      }
    }

    it("send content in seq") {
      val theServer = server(port) when {
        method("get")
      } respond {
        seq("foo", "bar", "baz")
      }

      theServer running {
        assert(get === "foo")
        assert(get === "bar")
        assert(get === "baz")
      }
    }

    it("send multi response handler") {
      val theServer = server(port) when {
        method("get")
      } respond {
        status(201) and text("hello")
      }

      theServer running {
        assert(getForStatus === 201)
        assert(get === "hello")
      }
    }

    it("send version") {
      val theServer = server(port) when {
        method("get")
      } respond {
        version("HTTP/1.0")
      }

      theServer running {
        val version = getForVersion

        assert(version.getMajor === 1)
        assert(version.getMinor === 0)
      }
    }
  }
}