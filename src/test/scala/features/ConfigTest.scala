package features

import org.scalatest.{FeatureSpec, FunSpec, GivenWhenThen}
import org.treppo.mocoscala.dsl.Conversions._
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class ConfigTest extends FunSpec with RemoteTestHelper {

  override val port = 8082

  describe("file root configuration") {
    it("serves files from file root") {
      val theServer = server(port) configs {
        fileRoot("src/test/resources")
      } when {
        method("get")
      } respond {
        file("bar.response")
      }

      theServer running {
        assert(get === "bar")
      }
    }
  }

  describe("context configuration") {
    it("responds in configured context") {
      val theServer = server(port) configs {
        context("/hello")
      } when {
        method("get")
      } respond {
        text("world")
      }

      theServer running {
        assert(get("/hello") === "world")
      }
    }
  }
}
