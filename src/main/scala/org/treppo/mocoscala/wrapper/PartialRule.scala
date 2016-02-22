package org.treppo.mocoscala.wrapper

import com.github.dreamhead.moco.{RequestMatcher, ResponseHandler}
import org.treppo.mocoscala.dsl.Moco

class PartialRule(matcher: RequestMatcher, moco: Moco) {

  def respond(handler: ResponseHandler): Moco =
    moco.record(new Rule(Some(matcher), handler))
}
