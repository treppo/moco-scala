package org.treppo.mocoscala.helper

import org.apache.http.{ProtocolVersion, HttpVersion}
import org.apache.http.client.fluent.Request
import org.apache.http.message.BasicNameValuePair

trait RemoteTestHelper {

  val port: Int
  private lazy val root = s"http://localhost:$port"

  def get: String = content(Request.Get(root))

  def get(uri: String): String = content(Request.Get(root + uri))

  def post(body: String): String = postBody(root, body)

  def post(uri: String, body: String): String =
    postBody(root + uri, body)

  def postForm(form: (String, String)) =
    content(Request.Post(root).bodyForm(new BasicNameValuePair(form._1, form._2)))

  def put(uri: String) = content(Request.Put(uri))

  def delete(uri: String) = content(Request.Delete(uri))

  def getForStatus = status(Request.Get(root))

  def getForStatus(uri: String) = status(Request.Get(root + uri))

  def getWithHeaders(headers: (String, String)*) = {
    val get = Request.Get(root)
    headers.foreach { case (name, value) => get.addHeader(name, value) }
    content(get)
  }

  def getForStatusWithCookie(cookie: (String, String)) = cookie match {
    case (name, value) => status(Request.Get(root).addHeader("Cookie", s"$name=$value"))
  }

  def getForStatusWithHeaders(headers: (String, String)*) = {
    val get = Request.Get(root)
    headers.foreach { case (name, value) => get.addHeader(name, value) }
    status(get)
  }

  def getForHeader(headerName: String): String = {
    Request.Get(root).execute.returnResponse.getFirstHeader(headerName).getValue
  }

  def getWithVersion(version: HttpVersion): String =
    Request.Get(root).version(version).execute.returnContent.asString

  def getForVersion: ProtocolVersion =
    Request.Get(root).execute.returnResponse.getProtocolVersion

  private def content(request: Request): String =
    request.execute().returnContent().asString

  private def postBody(uri: String, body: String) =
    Request.Post(uri).bodyByteArray(body.getBytes).execute().returnContent().asString()

  private def status(request: Request): Int = {
    request.execute.returnResponse.getStatusLine.getStatusCode
  }
}
