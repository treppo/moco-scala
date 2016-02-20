package features

import com.github.dreamhead.moco.MocoEventAction
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.treppo.mocoscala.dsl.Conversions._
import org.treppo.mocoscala.dsl.SMoco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class EventActionTest extends FunSpec with RemoteTestHelper with MockitoSugar {

  override val port = 8083

  describe("on complete") {

    it("perform predefined action") {
      val theServer = server(port)

      val action = mock[MocoEventAction]

      theServer default {
        text("foo")
      } on {
        complete(action)
      }

      theServer running {
        assert(get === "foo")
      }

      verify(action).execute()
    }

  }

}
