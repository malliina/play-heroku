package backend

import javax.net.ssl.{SSLSession, HostnameVerifier}

/**
 * @author Michael
 */
class MyHostnameVerifier extends HostnameVerifier {
  override def verify(s: String, sslSession: SSLSession): Boolean = true
}
