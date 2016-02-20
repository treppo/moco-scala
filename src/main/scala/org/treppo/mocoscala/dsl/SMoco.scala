package org.treppo.mocoscala.dsl

import java.util.concurrent.TimeUnit

import com.github.dreamhead.moco._
import com.github.dreamhead.moco.config.{MocoContextConfig, MocoFileRootConfig}
import com.github.dreamhead.moco.extractor.{ContentRequestExtractor, UriRequestExtractor}
import com.github.dreamhead.moco.handler.failover.Failover
import com.github.dreamhead.moco.handler.proxy.ProxyConfig
import com.github.dreamhead.moco.handler.{AndResponseHandler, SequenceContentHandler}
import com.github.dreamhead.moco.internal.{ActualHttpServer, MocoHttpServer}
import com.github.dreamhead.moco.procedure.LatencyProcedure
import com.github.dreamhead.moco.resource.{ContentResource, Resource}
import com.google.common.collect.ImmutableList
import com.google.common.net.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus
import org.treppo.mocoscala.dsl.Conversions.{CompositeMocoConfig, _}
import org.treppo.mocoscala.wrapper.{ExtractorMatcher, PartialRule, Rule}

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

object SMoco {

  def fileRoot(path: String): MocoConfig[_] = new MocoFileRootConfig(path)

  def context(context: String): MocoConfig[_] = new MocoContextConfig(context)

  def server(port: Int): SMoco = new SMoco(port)

  def uri(value: String): Resource = Moco.uri(value)

  def method(value: String): Resource = Moco.method(value)

  def matched(value: Resource) = Moco.`match`(value)

  def text(value: String): Resource = Moco.text(value)

  def file(filename: String): Resource = Moco.file(filename)

  def version(value: String): Resource = Moco.version(value)

  def header(name: String): ExtractorMatcher = new ExtractorMatcher(Moco.header(name))

  def query(name: String): ExtractorMatcher = new ExtractorMatcher(Moco.query(name))

  def cookie(name: String): ExtractorMatcher = new ExtractorMatcher(Moco.cookie(name))

  def uri: ExtractorMatcher = new ExtractorMatcher(new UriRequestExtractor)

  def text: ExtractorMatcher = new ExtractorMatcher(new ContentRequestExtractor)

  def form(key: String): ExtractorMatcher = new ExtractorMatcher(Moco.form(key))

  def xpath(path: String): ExtractorMatcher = new ExtractorMatcher(Moco.xpath(path))

  def jsonPath(path: String): ExtractorMatcher = new ExtractorMatcher(Moco.jsonPath(path))

  def xml(content: Resource): RequestMatcher = Moco.xml(content)

  def json(content: Resource): RequestMatcher = Moco.json(content)

  def latency(duration: Duration): LatencyProcedure = Moco.latency(duration.toMillis, TimeUnit.MILLISECONDS)

  def status(code: Int): ResponseHandler = Moco.status(code)

  def attachment(filename: String, resource: Resource) = Moco.attachment(filename, resource)

  def redirectTo(uri: String): ResponseHandler = status(HttpResponseStatus.FOUND.code()) and headers(HttpHeaders.LOCATION -> uri)

  def seq(resources: Resource*): ResponseHandler = {
    val handlers = ImmutableList.
      builder[ResponseHandler].
      addAll(resources.map(toHandler)).
      build
    new SequenceContentHandler(handlers)
  }

  def headers(headers: (String, String)*): ResponseHandler = {
    val handlers = headers.map { case (name, value) => Moco.header(name, value) }
    new AndResponseHandler(handlers)
  }

  def cookies(cookies: (String, String)*): ResponseHandler = {
    val handlers = cookies.map { case (name, value) => Moco.cookie(name, value) }
    new AndResponseHandler(handlers)
  }

  def proxy(url: String)(implicit failover: Failover) = Moco.proxy(url, failover)

  def proxy(config: => ProxyConfig) = Moco.proxy(config)

  def failover(filename: String): Failover = Moco.failover(filename)

  def playback(filename: String): Failover = Moco.playback(filename)

  def from(localBase: String) = Moco.from(localBase)

  def complete(action: MocoEventAction): MocoEventTrigger = Moco.complete(action)

  def async(action: MocoEventAction): MocoEventAction = Moco.async(action)

  def async(action: MocoEventAction, duration: Duration): MocoEventAction = Moco.async(action, latency(duration))

  def get(url: String) = Moco.get(url)

  def post(url: String, content: ContentResource) = Moco.post(url, content)
}


class SMoco(port: Int = 8080) {

  var triggers: List[MocoEventTrigger] = List()

  var confs: Seq[MocoConfig[_]] = Seq()

  var rules: List[Rule] = List()

  def running[T](testFun: => T): T = {
    val theServer = startServer
    try {
      testFun
    } finally {
      theServer.stop()
    }
  }

  def when(matcher: RequestMatcher): PartialRule = new PartialRule(matcher, this)

  def default(handler: ResponseHandler): SMoco = {
    this.rules = Rule.default(handler) :: this.rules
    this
  }

  def configs(configsFun: => CompositeMocoConfig) {
    this.confs = configsFun.items
  }

  def on(trigger: MocoEventTrigger) {
    this.triggers = trigger :: this.triggers
  }

  def record(rule: Rule) = {
    this.rules = rule :: this.rules
  }

  private def startServer: MocoHttpServer = {
    val theServer = new MocoHttpServer(replay)
    theServer.start()
    theServer
  }

  private def replay: ActualHttpServer = {
    val server = confs match {
      case confs: Seq[MocoConfig[_]] => Moco.httpServer(port, confs: _*).asInstanceOf[ActualHttpServer]
      case _ => Moco.httpServer(port).asInstanceOf[ActualHttpServer]
    }

    rules.foreach {
      rule: Rule =>
        rule.matcher match {
          case Some(matcher) => server.request(matcher).response(rule.handler)
          case None => server.response(rule.handler)
        }
    }

    triggers.foreach(server.on)

    server
  }

}