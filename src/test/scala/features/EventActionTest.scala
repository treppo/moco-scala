package features

import com.github.dreamhead.moco.MocoEventAction
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSpec}
import org.treppo.mocoscala.dsl.Conversions._
import org.treppo.mocoscala.dsl.SMoco
import org.treppo.mocoscala.dsl.SMoco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class EventActionTest extends FunSpec with BeforeAndAfter with RemoteTestHelper with MockitoSugar {
  override val port: Int = 8083

  var theServer: SMoco = null

  before {
    theServer = server(port)
  }

  describe("on complete") {

    it("perform predefined action") {

      val action = mock[MocoEventAction]

      theServer default {
        text("foo")
      } on {
        complete(action)
      }

      theServer running {
        assert(get(root) === "foo")
      }

      verify(action).execute()
    }

  }

}
