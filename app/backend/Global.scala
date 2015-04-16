package backend

import com.mle.util.Log
import play.api.{Application, GlobalSettings}

/**
 * @author Michael
 */
object Global extends GlobalSettings with Log {
  override def onStart(app: Application): Unit = {
    super.onStart(app)
  }
}
