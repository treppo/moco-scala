package org.treppo.mocoscala

import java.io.Closeable
import java.net.ServerSocket

object RandomPort {
  def get: Int = {
    using(new ServerSocket(0)) { socket =>
      socket.setReuseAddress(true)
      socket.getLocalPort
    }
  }

  private def using[A <: Closeable, B](resource: A)(block: A => B): B = {
    try {
      block(resource)
    } finally {
      if (resource != null) resource.close()
    }
  }
}
