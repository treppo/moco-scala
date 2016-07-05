package org.treppo.mocoscala
package dsl

import java.util.concurrent.TimeUnit

import com.github.dreamhead.moco.action.MocoAsyncAction
import com.github.dreamhead.moco.config.{MocoContextConfig, MocoFileRootConfig}
import com.github.dreamhead.moco._
import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.treppo.mocoscala.dsl.Moco._
import org.treppo.mocoscala.wrapper.ExtractorMatcher

import scala.concurrent.duration.{Duration, FiniteDuration}

class MocoTest extends FlatSpec with MockitoSugar {

  "a config api" should "capture multiple configs" in {
    val conf1 = mock[MocoConfig[_]]
    val conf2 = mock[MocoConfig[_]]
    val configs = CompositeMocoConfig(Seq(conf1, conf2))

    val server = Moco().configs(configs)

    server.confs should equal(Seq(conf1, conf2))
  }

  "an event handler" should "record event triggers" in {
    val trigger = mock[MocoEventTrigger]

    val server = Moco().on(trigger)

    server.triggers should contain(trigger)
  }


  "a file root config api" should "generate a file root config" in {
    val config = Moco.fileRoot("root")

    config shouldBe a[MocoFileRootConfig]
  }

  "a context config api" should "generate a context config" in {
    val config = Moco.context("hello")

    config shouldBe a[MocoContextConfig]
  }

  "a form matcher" should "be a extractor matcher" in {
    Moco.form("name") shouldBe a[ExtractorMatcher]
  }

  "a xml matcher" should "be a request matcher" in {
    Moco.xml(Moco.file("filename")) shouldBe a[RequestMatcher]
  }

  "a xpath matcher" should "be a extractor matcher" in {
    Moco.xpath("/request/parameters/id/text()") shouldBe a[ExtractorMatcher]
  }

  "a json matcher" should "be a request matcher" in {
    Moco.json(Moco.file("filename")) shouldBe a[RequestMatcher]
  }

  "a jsonpath matcher" should "be a extractor matcher" in {
    Moco.jsonPath("$.book[*].price") shouldBe a[ExtractorMatcher]
  }

  "a proxy" should "be a response handler" in {
    Moco.proxy("http://github.com") shouldBe a[ResponseHandler]
  }

  "a proxy with failover" should "be a response handler" in {
    Moco.proxy("http://github.com") {
      Moco.failover("failover-filename")
    } shouldBe a[ResponseHandler]
  }

  "a proxy with playback" should "be a response handler" in {
    Moco.proxy("http://github.com") {
      Moco.playback("playback-filename")
    } shouldBe a[ResponseHandler]
  }

  "a proxy with from and to" should "be a response handler" in {
    Moco.proxy {
      Moco.from("local-base") to "remote-base"
    } shouldBe a[ResponseHandler]
  }

  "a attachment" should "be a response handler" in {
    Moco.attachment("attachment-file-name", Moco.file("filename")) shouldBe a[ResponseHandler]
  }

  "a complete trigger" should "be moco event trigger" in {
    val action = mock[MocoEventAction]
    Moco.complete(action) shouldBe a[MocoEventTrigger]
  }

  "a async action" should "be a moco event action" in {
    val action = mock[MocoEventAction]
    Moco.async(action) shouldBe a[MocoEventAction]
  }

  "a async action" should "be a moco async event action" in {
    val action = mock[MocoEventAction]
    Moco.async(action) shouldBe a[MocoAsyncAction]
  }

  "a async action with latency" should "be a moco async event action" in {
    val action = mock[MocoEventAction]
    val duration: FiniteDuration = Duration(2, TimeUnit.SECONDS)

    Moco.async(action, duration) shouldBe a[MocoAsyncAction]
  }
}
