package controllers

import backend.Conf
import com.mle.util.Log
import play.api.mvc._

/**
 *
 * @author mle
 */
object Home extends Controller with Log {
  def index = whitelisted {
    Action(request => {
      val remoteAddress = request.headers.getAll(X_FORWARDED_FOR).lastOption getOrElse request.remoteAddress
      Ok(views.html.index(remoteAddress))
    })
  }

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
