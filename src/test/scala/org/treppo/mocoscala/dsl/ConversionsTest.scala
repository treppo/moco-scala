package org.treppo.mocoscala.dsl

import com.github.dreamhead.moco.{MocoConfig, MocoProcedure, ResponseHandler}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar
import org.treppo.mocoscala.dsl.Conversions._

class ConversionsTest extends FlatSpec with MockitoSugar {

  "a moco config" should "compose with another moco config" in {
    val conf1 = mock[MocoConfig[_]]
    val conf2 = mock[MocoConfig[_]]
    val configs = conf1 and conf2

    configs shouldBe a [CompositeMocoConfig]
    configs.items should contain allOf(conf1, conf2)
  }

  "a moco config" should "convert to a composite config" in {
    val configs: CompositeMocoConfig = mock[MocoConfig[_]]
    configs shouldBe a [CompositeMocoConfig]
  }

  "a procedure" should "convert to a response handler" in {
    val procedure: ResponseHandler = mock[MocoProcedure]
    procedure shouldBe a [ResponseHandler]

  }
}
