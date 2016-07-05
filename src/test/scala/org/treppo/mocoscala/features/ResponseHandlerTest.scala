package org.treppo.mocoscala.features

import java.net.URI
import java.util.concurrent.TimeUnit

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

import scala.concurrent.duration.Duration

class ResponseHandlerTest extends FunSpec with BeforeAndAfter with RemoteTestHelper {

  describe("default") {
    it("send default response") {
      val theServer = server respond {
        text("default")
      }

      theServer running { uri: URI =>
        assert(get(uri) === "default")
      }
    }
  }

  describe("redirect") {
    it("redirect to expected url") {
      val theServer = server when {
        uri("/")
      } respond {
        text("foo")
      } when {
        uri("/redirect")
      } respond {
        redirectTo("/")
      }

      theServer running { uri: URI =>
        assert(get(uri.resolve("/redirect")) === "foo")
      }
    }
  }

  describe("latency") {
    it("wait for a while") {
      val duration = Duration(1, TimeUnit.SECONDS)

      val theServer = server respond {
        latency(duration)
      }

      theServer running { uri: URI =>
        val start = System.currentTimeMillis()
        getForStatus(uri)
        val stop = System.currentTimeMillis()

        assert((stop - start) >= duration.toMillis)
      }
    }
  }

  describe("responses") {
    it("send text") {
      val theServer = server when {
        method("get")
      } respond {
        text("get")
      }

      theServer running { uri: URI =>
        assert(get(uri) === "get")
      }
    }

    it("send headers") {
      val theServer = server when {
        method("get")
      } respond {
        headers("Content-Type" -> "json", "Accept" -> "html")
      }

      theServer running { uri: URI =>
        assert(getForHeader(uri, "Content-Type") === "json")
        assert(getForHeader(uri, "Accept") === "html")
      }
    }

    it("send content in seq") {
      val theServer = server when {
        method("get")
      } respond {
        seq("foo", "bar", "baz")
      }

      theServer running { uri: URI =>
        assert(get(uri) === "foo")
        assert(get(uri) === "bar")
        assert(get(uri) === "baz")
      }
    }

    it("send multi response handler") {
      val theServer = server when {
        method("get")
      } respond {
        status(201) and text("hello")
      }

      theServer running { uri: URI =>
        assert(getForStatus(uri) === 201)
        assert(get(uri) === "hello")
      }
    }

    it("send version") {
      val theServer = server when {
        method("get")
      } respond {
        version("HTTP/1.0")
      }

      theServer running { uri: URI =>
        val version = getForVersion(uri)

        assert(version.getMajor === 1)
        assert(version.getMinor === 0)
      }
    }
  }
}
