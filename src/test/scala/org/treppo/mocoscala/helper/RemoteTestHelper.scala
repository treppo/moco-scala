package org.treppo.mocoscala.helper

import java.net.URI

import org.apache.http.client.fluent.{Request, Response}
import org.apache.http.entity.ContentType
import org.apache.http.message.BasicNameValuePair
import org.apache.http.{HttpVersion, ProtocolVersion}

trait RemoteTestHelper {

  val emptyBody = ""

  def getRoot(uri: URI): String = content(executeGet(uri))

  def get(uri: URI): String = content(executeGet(uri))

  def post(uri: URI, body: String): String = postBody(uri, body)

  def post(uri: URI): String = postBody(uri, emptyBody)

  def postForm(uri: URI, form: (String, String)) = form match {
    case (name, value) =>
      content(Request.Post(uri).bodyForm(new BasicNameValuePair(name, value)).execute)
  }

  def postXmlForStatus(uri: URI, xml: String): Int =
    status(Request.Post(uri).bodyString(xml, ContentType.TEXT_XML).execute)

  def postJsonForStatus(uri: URI, json: String): Int =
    status(Request.Post(uri).bodyString(json, ContentType.APPLICATION_JSON).execute)

  def getForStatus(uri: URI) = status(executeGet(uri))

  def getWithHeaders(uri: URI, headers: (String, String)*) = {
    val get = Request.Get(uri)
    headers.foreach { case (name, value) => get.addHeader(name, value) }
    content(get.execute)
  }

  def getForStatusWithCookie(uri: URI, cookie: (String, String)) = cookie match {
    case (name, value) => status(Request.Get(uri).addHeader("Cookie", s"$name=$value").execute)
  }

  def getForHeader(uri: URI, headerName: String): String = {
    executeGet(uri).returnResponse.getFirstHeader(headerName).getValue
  }

  def getWithVersion(uri: URI, version: HttpVersion): String =
    Request.Get(uri).version(version).execute.returnContent.asString

  def getForVersion(uri: URI): ProtocolVersion =
    executeGet(uri).returnResponse.getProtocolVersion

  private def content(response: Response): String =
    response.returnContent().asString

  private def postBody(uri: URI, body: String) =
    content(Request.Post(uri).bodyByteArray(body.getBytes).execute)

  private def status(response: Response): Int =
    response.returnResponse.getStatusLine.getStatusCode

  private def executeGet(uri: URI): Response =
    Request.Get(uri).execute
}
