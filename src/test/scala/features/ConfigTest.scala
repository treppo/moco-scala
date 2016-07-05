package features

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

      theServer running { url: String =>
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

      theServer running { url: String =>
        assert(get(url + "/hello") === "world")
      }
    }
  }
}
