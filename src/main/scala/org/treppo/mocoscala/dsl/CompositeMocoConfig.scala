package org.treppo.mocoscala
package dsl

import com.github.dreamhead.moco.MocoConfig

case class CompositeMocoConfig(items: Seq[MocoConfig[_]]) {
  def and(config: MocoConfig[_]): CompositeMocoConfig = copy(config +: items)
}
