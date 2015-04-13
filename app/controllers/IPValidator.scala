package controllers

import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.validator.routines.InetAddressValidator

import scala.util.{Failure, Try}

/**
 * @author Michael
 */
case class IPValidator(validators: Seq[SubnetUtils#SubnetInfo], ips: Seq[IP]) {
  def isValid(ip: String) = validators.exists(_.isInRange(ip)) || ips.exists(_.ip == ip)
}

object IPValidator {
  def fromList(whitelist: Seq[String]): Try[IPValidator] = {
    val errors = whitelist filterNot isRangeOrIP
    if (errors.nonEmpty) {
      val errorFormatted = errors mkString ", "
      Failure(new IllegalArgumentException(s"Invalid input: $errorFormatted"))
    } else {
      val (singleIPs, maybeRanges) = whitelist partition isIP
      Try {
        val rangeValidators = maybeRanges.map(maybeRange => {
          // might throw
          val utils = new SubnetUtils(maybeRange)
          utils setInclusiveHostCount true
          utils.getInfo
        })
        val singles = singleIPs map (ip => IP.fromString(ip).getOrElse(throw new IllegalArgumentException(s"Invalid IP: $ip")))
        IPValidator(rangeValidators, singles)
      }
    }
  }

  private val validator = InetAddressValidator.getInstance()

  def isRangeOrIP(in: String) = isIP(in) || isRange(in)

  def isIP(in: String): Boolean = validator isValid in

  def isRange(in: String) = {
    in.split('/') match {
      case Array(ip, mask) => isIP(ip) && Try(mask.toInt > 0 && mask.toInt <= 32).isSuccess
      case _ => false
    }
  }
}
