package org.treppo.mocoscala.features

import java.net.URI

import org.scalatest.FunSpec
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class PortTest extends FunSpec with RemoteTestHelper {

  describe("Moco server with random port") {
    it("can be run in parallel without port binding collisions") {
      val serverA = server when {
        method("get")
      } respond {
        text("server a")
      }

      val serverB = server when {
        method("get")
      } respond {
        text("server b")
      }

      serverA running { urlA: URI =>
        serverB running { urlB: URI =>
          assert(get(urlB) === "server b")
        }
      }
    }
  }

  describe("Moco server") {
    it("can be run with a predefined port") {
      val port = 8085
      val theServer = server(port) when {
        method("get")
      } respond {
        text("server a")
      }

      theServer running { uri: URI =>
        assert(getRoot(uri) === "server a")
      }
    }
  }
}
