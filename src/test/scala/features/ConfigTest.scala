package features

import org.scalatest.{BeforeAndAfter, FeatureSpec, GivenWhenThen}
import org.treppo.mocoscala.dsl.Conversions._
import org.treppo.mocoscala.dsl.SMoco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class ConfigTest extends FeatureSpec
with GivenWhenThen with BeforeAndAfter
with RemoteTestHelper {

  info("As a api consumer")
  info("I want to be able to config my mock server")
  info("So I these configs can be shared by all examples")

  val port = 8082

  feature("config file root") {

    scenario("serving file under configured file root") {

      Given("The server was configured with file root")
      val theServer = server(port)
      theServer configs {
        fileRoot("src/test/resources")
      }

      When("The server serving requests")
      theServer when {
        method("get")
      } then {
        file("bar.response")
      }


      Then("The response file should be served under configured file root")
      theServer running {
        assert(get(remoteUrl("/")) === "bar")
      }
    }
  }

  feature("config context") {

    scenario("serving requests under configured context") {

      Given("The server was configured with context")
      val theServer = server(port)
      theServer configs {
        context("/hello")
      }

      When("The server serving requests")
      theServer when {
        method("get")
      } then {
        text("world")
      }

      Then("The response should be served under configured context")
      theServer running {
        assert(get(remoteUrl("/hello")) === "world")
      }
    }
  }
}
