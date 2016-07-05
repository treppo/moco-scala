package features

import java.net.URI

import com.github.dreamhead.moco.MocoEventAction
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.helper.RemoteTestHelper

class EventActionTest extends FunSpec with RemoteTestHelper with MockitoSugar {

  describe("on complete") {

    it("perform predefined action") {
      val action = mock[MocoEventAction]

      val theServer = server respond {
        text("foo")
      } on {
        complete(action)
      }

      theServer running { url: URI =>
        assert(get(url) === "foo")
      }

      verify(action).execute()
    }

  }

}
