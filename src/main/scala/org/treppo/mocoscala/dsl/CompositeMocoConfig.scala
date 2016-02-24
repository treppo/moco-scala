package org.treppo.mocoscala.dsl

import com.github.dreamhead.moco.{Moco => JMoco, _}

case class CompositeMocoConfig(items: Seq[MocoConfig[_]]) {
  def and(config: MocoConfig[_]): CompositeMocoConfig = new CompositeMocoConfig(config +: items)
}
