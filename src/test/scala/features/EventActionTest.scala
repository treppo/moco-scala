package features

import com.github.dreamhead.moco.MocoEventAction
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class EventActionTest extends FunSpec with RemoteTestHelper with MockitoSugar {

  override val port = 8083

  describe("on complete") {

    it("perform predefined action") {
      val action = mock[MocoEventAction]

      val theServer = server(port) respond {
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
