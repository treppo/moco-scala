package org.treppo.mocoscala
package dsl

import java.util.concurrent.TimeUnit

import com.github.dreamhead.moco.config.{MocoContextConfig, MocoFileRootConfig}
import com.github.dreamhead.moco.extractor.{ContentRequestExtractor, UriRequestExtractor}
import com.github.dreamhead.moco.handler.AndResponseHandler
import com.github.dreamhead.moco.handler.failover.Failover
import com.github.dreamhead.moco.handler.proxy.ProxyConfig
import com.github.dreamhead.moco.internal.{ActualHttpServer, MocoHttpServer}
import com.github.dreamhead.moco.matcher.AndRequestMatcher
import com.github.dreamhead.moco.procedure.LatencyProcedure
import com.github.dreamhead.moco.resource.{ContentResource, Resource}
import com.github.dreamhead.moco.{Moco => JMoco, _}
import com.google.common.net.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus
import org.treppo.mocoscala.wrapper.{ExtractorMatcher, PartialRule, Rule}

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

object Moco {

  implicit def toResource(text: String): Resource = JMoco.text(text)

  implicit def toMatcher(resource: Resource): RequestMatcher = JMoco.`match`(resource)

  implicit def toHandler(resource: Resource): ResponseHandler = JMoco.`with`(resource)

  implicit def toHandler(procedure: MocoProcedure): ResponseHandler = JMoco.`with`(procedure)

  implicit def toCompositeMocoConfig(config: MocoConfig[_]): CompositeMocoConfig = CompositeMocoConfig(Seq(config))

  implicit val failover: Failover = Failover.DEFAULT_FAILOVER

  implicit class RichResource(target: Resource) {
    def and(handler: ResponseHandler): ResponseHandler = AndResponseHandler.and(Seq[ResponseHandler](handler, target))

    def and(matcher: RequestMatcher): RequestMatcher = JMoco.and(matcher)

    def and(resource: Resource): RequestMatcher = new AndRequestMatcher(Seq[RequestMatcher](resource, target))
  }

  implicit class RichRequestMatcher(target: RequestMatcher) {
    def and(matcher: RequestMatcher): RequestMatcher = new AndRequestMatcher(Seq(matcher, target))
  }

  implicit class RichResponseHandler(target: ResponseHandler) {
    def and(handler: ResponseHandler): ResponseHandler = AndResponseHandler.and(Seq(handler, target))
  }

  def fileRoot(path: String): MocoConfig[_] = new MocoFileRootConfig(path)

  def context(context: String): MocoConfig[_] = new MocoContextConfig(context)

  def server(port: Int): Moco = Moco(port)

  def uri(value: String): Resource = JMoco.uri(value)

  def method(value: String): Resource = JMoco.method(value)

  def matched(value: Resource) = JMoco.`match`(value)

  def text(value: String): Resource = JMoco.text(value)

  def file(filename: String): Resource = JMoco.file(filename)

  def version(value: String): Resource = JMoco.version(value)

  def header(name: String): ExtractorMatcher = ExtractorMatcher(JMoco.header(name))

  def query(name: String): ExtractorMatcher = ExtractorMatcher(JMoco.query(name))

  def cookie(name: String): ExtractorMatcher = ExtractorMatcher(JMoco.cookie(name))

  def uri: ExtractorMatcher = ExtractorMatcher(new UriRequestExtractor)

  def text: ExtractorMatcher = ExtractorMatcher(new ContentRequestExtractor)

  def form(key: String): ExtractorMatcher = ExtractorMatcher(JMoco.form(key))

  def xpath(path: String): ExtractorMatcher = ExtractorMatcher(JMoco.xpath(path))

  def jsonPath(path: String): ExtractorMatcher = ExtractorMatcher(JMoco.jsonPath(path))

  def xml(content: Resource): RequestMatcher = JMoco.xml(content)

  def json(content: Resource): RequestMatcher = JMoco.json(content)

  def latency(duration: Duration): LatencyProcedure = JMoco.latency(duration.toMillis, TimeUnit.MILLISECONDS)

  def status(code: Int): ResponseHandler = JMoco.status(code)

  def attachment(filename: String, resource: Resource) = JMoco.attachment(filename, resource)

  def redirectTo(uri: String): ResponseHandler =
    status(HttpResponseStatus.FOUND.code()) and headers(HttpHeaders.LOCATION -> uri)

  def seq(resources: Resource*): ResponseHandler = JMoco.seq(resources.map(toHandler): _*)

  def headers(headers: (String, String)*): ResponseHandler = {
    val handlers = headers.map { case (name, value) => JMoco.header(name, value) }
    AndResponseHandler.and(handlers)
  }

  def cookies(cookies: (String, String)*): ResponseHandler = {
    val handlers = cookies.map { case (name, value) => JMoco.cookie(name, value) }
    AndResponseHandler.and(handlers)
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

  def respond(handler: ResponseHandler): Moco = copy(rules = Rule.default(handler) :: rules)

  def configs(configsFun: => CompositeMocoConfig): Moco = copy(confs = configsFun.items)

  def on(trigger: MocoEventTrigger): Moco = copy(triggers = trigger :: triggers)

  def record(rule: Rule): Moco = copy(rules = rule :: rules)

  private def startServer: MocoHttpServer = {
    val theServer = new MocoHttpServer(replay)
    theServer.start()
    theServer
  }

  private def replay: ActualHttpServer = {
    val server =
      if (confs.isEmpty) JMoco.httpServer(port).asInstanceOf[ActualHttpServer]
      else JMoco.httpServer(port, confs: _*).asInstanceOf[ActualHttpServer]

    rules.foreach {
      case Rule(Some(matcher), handler) => server.request(matcher).response(handler)
      case Rule(None, handler) => server.response(handler)
    }

    triggers.foreach(server.on)

    server
  }

}
