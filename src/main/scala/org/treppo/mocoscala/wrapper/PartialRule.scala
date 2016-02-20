package org.treppo.mocoscala.wrapper

import com.github.dreamhead.moco.{RequestMatcher, ResponseHandler}
import org.treppo.mocoscala.dsl.SMoco

class PartialRule(matcher: RequestMatcher, moco: SMoco) {

  def then(handler: ResponseHandler): SMoco = {
    moco.record(new Rule(Some(matcher), handler))
    moco
  }
}
