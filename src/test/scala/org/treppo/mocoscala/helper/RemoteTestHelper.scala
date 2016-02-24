package org.treppo.mocoscala.helper

import org.apache.http.client.fluent.{Request, Response}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.{HttpVersion, ProtocolVersion}

trait RemoteTestHelper {

  val port: Int
  private lazy val root = s"http://localhost:$port"

  def get: String = content(Request.Get(root).execute)

  def get(uri: String): String = content(Request.Get(root + uri).execute)

  def post(body: String): String = postBody(root, body)

  def post(uri: String, body: String): String =
    postBody(root + uri, body)

  def postForm(form: (String, String)) =
    content(Request.Post(root).bodyForm(new BasicNameValuePair(form._1, form._2)).execute)

  def put(uri: String) = content(Request.Put(uri).execute)

  def delete(uri: String) = content(Request.Delete(uri).execute)

  def getForStatus = status(Request.Get(root).execute)

  def getForStatus(uri: String) = status(Request.Get(root + uri).execute)

  def getWithHeaders(headers: (String, String)*) = {
    val get = Request.Get(root)
    headers.foreach { case (name, value) => get.addHeader(name, value) }
    content(get.execute)
  }

  def getForStatusWithCookie(cookie: (String, String)) = cookie match {
    case (name, value) => status(Request.Get(root).addHeader("Cookie", s"$name=$value").execute)
  }

  def getForStatusWithHeaders(headers: (String, String)*) = {
    val get = Request.Get(root)
    headers.foreach { case (name, value) => get.addHeader(name, value) }
    status(get.execute)
  }

  def getForHeader(headerName: String): String = {
    Request.Get(root).execute.returnResponse.getFirstHeader(headerName).getValue
  }

  def getWithVersion(version: HttpVersion): String =
    Request.Get(root).version(version).execute.returnContent.asString

  def getForVersion: ProtocolVersion =
    Request.Get(root).execute.returnResponse.getProtocolVersion

  private def content(response: Response): String =
    response.returnContent().asString

  private def postBody(uri: String, body: String) =
    content(Request.Post(uri).bodyByteArray(body.getBytes).execute)

  private def status(response: Response): Int = {
    response.returnResponse.getStatusLine.getStatusCode
  }
}
