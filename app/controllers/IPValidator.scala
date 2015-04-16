package controllers

import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.validator.routines.InetAddressValidator

import scala.util.{Failure, Success, Try}

/**
 * @author Michael
 */
trait IIPValidator {
  def isValid(ip: String): Boolean

  def describe: String
}

case class IPValidator(validators: Seq[SubnetUtils#SubnetInfo], singles: Seq[IP]) extends IIPValidator {
  def isValid(ip: String) = validators.exists(_.isInRange(ip)) || singles.exists(_.ip == ip)

  override def describe: String = (validators.map(_.getCidrSignature) ++ singles.map(_.ip)) mkString ", "
}

object IPValidator {
  val allowAll = new IIPValidator {
    override def isValid(ip: String): Boolean = true

    override def describe: String = "allow all"
  }
  val forbidAll = new IIPValidator {
    override def isValid(ip: String): Boolean = false

    override def describe: String = "forbid all"
  }

  def fromList(whitelist: Seq[String]): Try[IIPValidator] = {
    if (whitelist.isEmpty) {
      Success(allowAll)
    } else {
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
  }

  def fromListOrForbid(whitelist: Seq[String]) = fromList(whitelist) getOrElse forbidAll

  private val addressValidator = InetAddressValidator.getInstance()

  def isRangeOrIP(in: String) = isIP(in) || isRange(in)

  def isIP(in: String): Boolean = addressValidator isValid in

  def isRange(in: String) = {
    in.split('/') match {
      case Array(ip, mask) => isIP(ip) && Try(mask.toInt > 0 && mask.toInt <= 32).isSuccess
      case _ => false
    }
  }
}
