package tests

import controllers.IPValidator
import org.apache.commons.net.util.SubnetUtils
import org.scalatest.FunSuite

/**
 * @author Michael
 */
class Ranges extends FunSuite {
  test("ranges") {
    val sub = new SubnetUtils("10.0.0.1/16").getInfo
    assert(sub isInRange "10.0.254.255")
    assert(sub isInRange "10.0.0.1")
    assert(sub isInRange "10.0.10.123")
    assert(!(sub isInRange "10.1.0.1"))
    assert(!(sub isInRange "192.168.0.1"))
  }
  test("singles") {
    val sub = {
      val ret = new SubnetUtils("10.2.3.1/32")
      ret.setInclusiveHostCount(true)
      ret.getInfo
    }
    assert(sub isInRange "10.2.3.1")
  }
  test("validator") {
    val validator = IPValidator.fromList(Seq("123.4.1.2/32", "212.2.4.5", "10.0.0.1/16")).get
    assert(validator isValid "10.0.0.1")
    assert(validator isValid "10.0.126.123")
    assert(validator isValid "212.2.4.5")
    assert(validator isValid "123.4.1.2")
    assert(!(validator isValid "212.2.4.6"))
    assert(!(validator isValid "10.1.0.1"))

    val dismissive = IPValidator.fromList(Nil).get
    assert(!(dismissive isValid "10.0.0.1"))
  }
}
