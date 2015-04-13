package controllers

import play.api.mvc._

/**
 *
 * @author mle
 */
object Home extends Controller {
  def index = Action(request => {
    val remoteAddress = request.headers.getAll(X_FORWARDED_FOR).lastOption getOrElse request.remoteAddress
    Ok(views.html.index(remoteAddress))
  })
}
