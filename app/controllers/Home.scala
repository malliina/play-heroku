package controllers

import java.net.URLDecoder

import backend.Conf
import com.mle.concurrent.ExecutionContexts.cached
import com.mle.http.AsyncHttp
import com.mle.util.Log
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.libs.ws.WS
import play.api.mvc._
import views.html

import scala.concurrent.Future

/**
 *
 * @author mle
 */
object Home extends Controller with Log {

  case class Header(key: String, value: String)

  val URL = "url"
  val form = Form[String](URL -> nonEmptyText)

  def index = whitelisted {
    Action(request => {
      val remoteAddress = request.headers.getAll(X_FORWARDED_FOR).lastOption getOrElse request.remoteAddress
      Ok(html.index(remoteAddress))
    })
  }

  def headers = Action.async(implicit request => {
    val url = request.getQueryString(URL)//.map(enc => URLDecoder.decode(enc, "UTF-8"))
    val responseHeaders = url.map(u => WS.url(u).get().map(resp => resp.allHeaders.map(kv => Header(kv._1, kv._2.mkString(", ")))))
      .getOrElse(Future.successful(Nil))
    responseHeaders.map(headers => Ok(html.headers(form, headers.toSeq)))
  })

  def resolveHeaders = Action(implicit request => {
    form.bindFromRequest.fold(
      erroredForm => {
        BadRequest(html.headers(erroredForm, Nil))
      },
      okForm => {
        Redirect(routes.Home.headers())
      }
    )
  })

  def whitelisted(a: => EssentialAction) = withWhitelist(Conf.validator)(a)

  def withWhitelist(validator: IIPValidator)(a: => EssentialAction) = EssentialAction(request => {
    val ip = remoteAddress(request)
    val action =
      if (validator isValid ip) {
        a
      } else {
        log.warn(s"$ip not in whitelist")
        Action(Forbidden)
      }
    action(request)
  })

  def remoteAddress(request: RequestHeader): String =
    request.headers.getAll(X_FORWARDED_FOR).lastOption getOrElse request.remoteAddress
}
