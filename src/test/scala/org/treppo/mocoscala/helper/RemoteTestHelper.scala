package org.treppo.mocoscala.helper

import java.net.URI

import org.apache.http.client.fluent.{Request, Response}
import org.apache.http.entity.ContentType
import org.apache.http.message.BasicNameValuePair
import org.apache.http.{HttpVersion, ProtocolVersion}

trait RemoteTestHelper {

  val port: Int = 9999
  private def baseUrl(port: Int = port) = s"http://localhost:$port"

  def getRoot: String = content(executeGetPath())
  def getRoot(port: Int): String = content(executeGetPath(port = port))

  def getPath(uri: String): String = content(executeGetPath(uri))
  def get(uri: String): String = content(executeGet(uri))
  def get(uri: URI): String = content(executeGet(uri.toString))

  def post(body: String): String = postBody(baseUrl(), body)

  def postRoot: String = postBody(baseUrl(), "")

  def post(url: URI): String = postBody(url.toString, "")

  def post(uri: String, body: String): String =
    postBody(baseUrl() + uri, body)

  def postForm(form: (String, String)) = form match {
    case (name, value) =>
      content(Request.Post(baseUrl()).bodyForm(new BasicNameValuePair(name, value)).execute)
  }

  def postXmlForStatus(xml: String): Int =
      status(Request.Post(baseUrl()).bodyString(xml, ContentType.TEXT_XML).execute)

  def postJsonForStatus(json: String): Int =
      status(Request.Post(baseUrl()).bodyString(json, ContentType.APPLICATION_JSON).execute)

  def put(uri: String) = content(Request.Put(uri).execute)

  def delete(uri: String) = content(Request.Delete(uri).execute)

  def getForStatus = status(executeGetPath())

  def getForStatus(uri: String) = status(executeGetPath(uri))

  def getWithHeaders(headers: (String, String)*) = {
    val get = Request.Get(baseUrl())
    headers.foreach { case (name, value) => get.addHeader(name, value) }
    content(get.execute)
  }

  def getForStatusWithCookie(cookie: (String, String)) = cookie match {
    case (name, value) => status(Request.Get(baseUrl()).addHeader("Cookie", s"$name=$value").execute)
  }

  def getForStatusWithHeaders(headers: (String, String)*) = {
    val get = Request.Get(baseUrl())
    headers.foreach { case (name, value) => get.addHeader(name, value) }
    status(get.execute)
  }

  def getForHeader(headerName: String): String = {
    executeGetPath().returnResponse.getFirstHeader(headerName).getValue
  }

  def getWithVersion(version: HttpVersion): String =
    Request.Get(baseUrl()).version(version).execute.returnContent.asString

  def getForVersion: ProtocolVersion =
    executeGetPath().returnResponse.getProtocolVersion

  private def content(response: Response): String =
    response.returnContent().asString

  private def postBody(uri: String, body: String) =
    content(Request.Post(uri).bodyByteArray(body.getBytes).execute)

  private def status(response: Response): Int =
    response.returnResponse.getStatusLine.getStatusCode

  private def executeGetPath(uri: String = "", port: Int = port): Response =
    Request.Get(baseUrl(port) + uri).execute

  private def executeGet(url: String = ""): Response =
    Request.Get(url).execute
}
