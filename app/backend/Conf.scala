package backend

import com.mle.util.Log
import controllers.{IIPValidator, IPValidator}
import play.api.Play.current

import scala.util.{Failure, Success}

/**
 * @author Michael
 */
object Conf extends Log {
  val WHITELIST_KEY = "ip.whitelist"
  val whitelist = sys.env.get(WHITELIST_KEY).map(_.split(" ").toSeq) orElse
    current.configuration.getStringSeq(WHITELIST_KEY) getOrElse Nil
  val validator: IIPValidator = {
    IPValidator.fromList(whitelist) match {
      case Success(v) =>
        v
      case Failure(t) =>
        log info s"Unable to parse whitelist. ${t.getMessage}"
        IPValidator.forbidAll
    }
  }
  log info s"Using whitelist ${validator.describe}"
}
