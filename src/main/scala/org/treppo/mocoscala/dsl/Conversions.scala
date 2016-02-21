package org.treppo.mocoscala.dsl

import com.github.dreamhead.moco.{Moco => JMoco, _}
import com.github.dreamhead.moco.handler.AndResponseHandler
import com.github.dreamhead.moco.handler.failover.Failover
import com.github.dreamhead.moco.matcher.AndRequestMatcher
import com.github.dreamhead.moco.resource.Resource

import scala.collection.JavaConversions._
import scala.language.implicitConversions

object Conversions {
  implicit def toResource(text: String): Resource = JMoco.text(text)

  implicit def toMatcher(resource: Resource): RequestMatcher = JMoco.`match`(resource)

  implicit def toHandler(resource: Resource): ResponseHandler = JMoco.`with`(resource)

  implicit def toHandler(procedure: MocoProcedure): ResponseHandler = JMoco.`with`(procedure)

  implicit def toCompositeMocoConfig(config: MocoConfig[_]): CompositeMocoConfig = new CompositeMocoConfig(Seq(config))


  implicit val failover: Failover = Failover.DEFAULT_FAILOVER

  implicit class RichResource(target: Resource) {
    def and(handler: ResponseHandler): ResponseHandler = new AndResponseHandler(Seq[ResponseHandler](handler, target))

    def and(matcher: RequestMatcher): RequestMatcher = new AndRequestMatcher(Seq[RequestMatcher](matcher, target))

    def and(resource: Resource): RequestMatcher = new AndRequestMatcher(Seq[RequestMatcher](resource, target))
  }

  implicit class RichRequestMatcher(target: RequestMatcher) {
    def and(matcher: RequestMatcher): RequestMatcher = new AndRequestMatcher(Seq(matcher, target))
  }

  implicit class RichResponseHandler(target: ResponseHandler) {
    def and(handler: ResponseHandler): ResponseHandler = new AndResponseHandler(Seq(handler, target))
  }
}

case class CompositeMocoConfig(items: Seq[MocoConfig[_]]) {
  def and(config: MocoConfig[_]): CompositeMocoConfig = new CompositeMocoConfig(config +: items)
}