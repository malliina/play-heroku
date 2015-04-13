package controllers

/**
 * @author Michael
 */
case class IP private(ip: String)

object IP {
  def fromString(ip: String): Option[IP] = if (IPValidator.isIP(ip)) Some(new IP(ip)) else None
}
