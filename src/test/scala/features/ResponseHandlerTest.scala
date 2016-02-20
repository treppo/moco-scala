package features

import java.util.concurrent.TimeUnit

import org.apache.http.client.fluent.Request
import org.scalatest.{BeforeAndAfter, FunSpec}
import org.treppo.mocoscala.dsl.Conversions._
import org.treppo.mocoscala.dsl.SMoco
import org.treppo.mocoscala.dsl.SMoco._
import org.treppo.mocoscala.helper.RemoteTestHelper

import scala.concurrent.duration.Duration

class ResponseHandlerTest extends FunSpec with BeforeAndAfter with RemoteTestHelper {

  var theServer: SMoco = null

  val port = 8080

  before {
    theServer = server(port)
  }

  describe("default") {
    it("send default response") {
      theServer default {
        text("default")
      }

      theServer running {
        assert(get === "default")
      }
    }
  }

  describe("redirect") {
    it("redirect to expected url") {

      theServer when {
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

      theServer default {
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
      theServer when {
        method("get")
      } respond {
        text("get")
      }


      theServer running {
        assert(get === "get")
      }
    }

    it("send headers") {
      theServer when {
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
      theServer when {
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
      theServer when {
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
      theServer when {
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