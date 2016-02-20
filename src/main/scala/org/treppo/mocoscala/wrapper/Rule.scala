package org.treppo.mocoscala.wrapper

import com.github.dreamhead.moco.{RequestMatcher, ResponseHandler}

case class Rule(matcher: Option[RequestMatcher], handler: ResponseHandler)

object Rule{
  def default(handler: ResponseHandler) = Rule(None, handler)
}
