package org.treppo.mocoscala.dsl

import java.util.concurrent.TimeUnit

import com.github.dreamhead.moco.config.{MocoContextConfig, MocoFileRootConfig}
import com.github.dreamhead.moco.extractor.{ContentRequestExtractor, UriRequestExtractor}
import com.github.dreamhead.moco.handler.failover.Failover
import com.github.dreamhead.moco.handler.proxy.ProxyConfig
import com.github.dreamhead.moco.handler.{AndResponseHandler, SequenceContentHandler}
import com.github.dreamhead.moco.internal.{ActualHttpServer, MocoHttpServer}
import com.github.dreamhead.moco.procedure.LatencyProcedure
import com.github.dreamhead.moco.resource.{ContentResource, Resource}
import com.github.dreamhead.moco.{Moco => JMoco, _}
import com.google.common.collect.ImmutableList
import com.google.common.net.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus
import org.treppo.mocoscala.dsl.Conversions._
import org.treppo.mocoscala.wrapper.{ExtractorMatcher, PartialRule, Rule}

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

object Moco {

  def fileRoot(path: String): MocoConfig[_] = new MocoFileRootConfig(path)

  def context(context: String): MocoConfig[_] = new MocoContextConfig(context)

  def server(port: Int): Moco = Moco(port)

  def uri(value: String): Resource = JMoco.uri(value)

  def method(value: String): Resource = JMoco.method(value)

  def matched(value: Resource) = JMoco.`match`(value)

  def text(value: String): Resource = JMoco.text(value)

  def file(filename: String): Resource = JMoco.file(filename)

  def version(value: String): Resource = JMoco.version(value)

  def header(name: String): ExtractorMatcher = new ExtractorMatcher(JMoco.header(name))

  def query(name: String): ExtractorMatcher = new ExtractorMatcher(JMoco.query(name))

  def cookie(name: String): ExtractorMatcher = new ExtractorMatcher(JMoco.cookie(name))

  def uri: ExtractorMatcher = new ExtractorMatcher(new UriRequestExtractor)

  def text: ExtractorMatcher = new ExtractorMatcher(new ContentRequestExtractor)

  def form(key: String): ExtractorMatcher = new ExtractorMatcher(JMoco.form(key))

  def xpath(path: String): ExtractorMatcher = new ExtractorMatcher(JMoco.xpath(path))

  def jsonPath(path: String): ExtractorMatcher = new ExtractorMatcher(JMoco.jsonPath(path))

  def xml(content: Resource): RequestMatcher = JMoco.xml(content)

  def json(content: Resource): RequestMatcher = JMoco.json(content)

  def latency(duration: Duration): LatencyProcedure = JMoco.latency(duration.toMillis, TimeUnit.MILLISECONDS)

  def status(code: Int): ResponseHandler = JMoco.status(code)

  def attachment(filename: String, resource: Resource) = JMoco.attachment(filename, resource)

  def redirectTo(uri: String): ResponseHandler =
    status(HttpResponseStatus.FOUND.code()) and headers(HttpHeaders.LOCATION -> uri)

  def seq(resources: Resource*): ResponseHandler = {
    val handlers = ImmutableList.builder[ResponseHandler].addAll(resources.map(toHandler)).build
    new SequenceContentHandler(handlers)
  }

  def headers(headers: (String, String)*): ResponseHandler = {
    val handlers = headers.map { case (name, value) => JMoco.header(name, value) }
    new AndResponseHandler(handlers)
  }

  def cookies(cookies: (String, String)*): ResponseHandler = {
    val handlers = cookies.map { case (name, value) => JMoco.cookie(name, value) }
    new AndResponseHandler(handlers)
  }

  def proxy(url: String)(implicit failover: Failover) = JMoco.proxy(url, failover)

  def proxy(config: => ProxyConfig) = JMoco.proxy(config)

  def failover(filename: String): Failover = JMoco.failover(filename)

  def playback(filename: String): Failover = JMoco.playback(filename)

  def from(localBase: String) = JMoco.from(localBase)

  def complete(action: MocoEventAction): MocoEventTrigger = JMoco.complete(action)

  def async(action: MocoEventAction): MocoEventAction = JMoco.async(action)

  def async(action: MocoEventAction, duration: Duration): MocoEventAction = JMoco.async(action, latency(duration))

  def get(url: String) = JMoco.get(url)

  def post(url: String, content: ContentResource) = JMoco.post(url, content)
}


case class Moco(port: Int = 8080,
                triggers: List[MocoEventTrigger] = List(),
                confs: Seq[MocoConfig[_]] = Seq(),
                rules: List[Rule] = List()) {

  def running[T](testFun: => T): T = {
    val theServer = startServer
    try {
      testFun
    } finally {
      theServer.stop()
    }
  }

  def when(matcher: RequestMatcher): PartialRule = new PartialRule(matcher, this)

  def respond(handler: ResponseHandler): Moco =
    copy(rules = Rule.default(handler) :: rules)

  def configs(configsFun: => CompositeMocoConfig): Moco =
    copy(confs = configsFun.items)

  def on(trigger: MocoEventTrigger): Moco =
    copy(triggers = trigger :: triggers)

  def record(rule: Rule): Moco =
    copy(rules = rule :: rules)

  private def startServer: MocoHttpServer = {
    val theServer = new MocoHttpServer(replay)
    theServer.start()
    theServer
  }

  private def replay: ActualHttpServer = {
    val server = if (confs.isEmpty)
      JMoco.httpServer(port).asInstanceOf[ActualHttpServer]
    else
      JMoco.httpServer(port, confs: _*).asInstanceOf[ActualHttpServer]

    rules.foreach {
      case Rule(Some(matcher), handler) => server.request(matcher).response(handler)
      case Rule(None, handler) => server.response(handler)
    }

    triggers.foreach(server.on)

    server
  }

}