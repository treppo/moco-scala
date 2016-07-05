package org.treppo.mocoscala.features

import java.net.URI

import org.scalatest.FunSpec
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class ConfigTest extends FunSpec with RemoteTestHelper {

  describe("file root configuration") {
    it("serves files from file root") {
      val theServer = server configs {
        fileRoot("src/test/resources")
      } when {
        method("get")
      } respond {
        file("bar.response")
      }

      theServer running { url: URI =>
        assert(get(url) === "bar")
      }
    }
  }

  describe("context configuration") {
    it("responds in configured context") {
      val theServer = server configs {
        context("/hello")
      } when {
        method("get")
      } respond {
        text("world")
      }

      theServer running { url: URI =>
        assert(get(url.resolve("/hello")) === "world")
      }
    }
  }
}
