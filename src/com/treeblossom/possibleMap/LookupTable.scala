package com.treeblossom.gmutil

import scala.util.matching.Regex
import scala.collection.mutable.Map

/**
 * @author jdonald
 *
 */
class LookupTable(val name: String) {

  val RANGE_PATTERN: Regex = """\\[(.+)\\] - \\[(.+)\\]""".r
  var yAxisLabel: String = ""
  var xAxisLabel: Option[String] = None
  var oneDimMap: Map[String, String] = null
  var twoDimMap: Map[String, Map[String, String]] = null
  var yKeyUsesRange: Boolean = false
  var xKeyUsesRange: Boolean = false

  /**
   * @param yKey
   * @param value
   */
  def putVal(yKey: String, value: String): Unit = {
    if (oneDimMap == null) {
      oneDimMap = Map()
    }
    if (!yKeyUsesRange) {
      var matches: Option[Regex.Match] = RANGE_PATTERN.findFirstMatchIn(yKey)
      var lowVal: String = null
      var highVal: String = null
      matches.foreach { mach =>
        {
          lowVal = mach.group(1)
          highVal = mach.group(2)
          yKeyUsesRange = true
        }
      }
    }
    oneDimMap.put(yKey, value)
  }

  /**
   * @param yKey
   * @param xKey
   * @param value
   */
  def putVal(yKey: String, xKey: String, value: String): Unit = {
    if (twoDimMap == null) {
      twoDimMap = Map()
    }
    //TODO implement ranges
    if (!twoDimMap.contains(yKey)) {
      twoDimMap.put(yKey, Map())
    }
    twoDimMap.get(yKey).map(yEntry => {
      yEntry.put(xKey, value)
      twoDimMap.put(yKey, yEntry)
    })
  }

  /**
   * @param yKey
   * @return
   */
  def getVal(yKey: String): Option[String] = {
    if (yKeyUsesRange) {
      getYvalByRange(yKey)
    } else {
      oneDimMap.get(yKey)
    }
  }

  /**
   * @param yKey
   * @return
   */
  def getYvalByRange(yKey: String): Option[String] = {
    oneDimMap.keysIterator.map(key => {
      var matches: Option[Regex.Match] = RANGE_PATTERN.findFirstMatchIn(key)
      matches.foreach {
        mach =>
          {
            var lowVal = mach.group(1)
            var highVal = mach.group(2)
            if (yKey >= lowVal && yKey <= highVal) {
              Some(oneDimMap.get(key))
            }
          }
      }
    })
    None
  }

  def getValByRange(key: String, theMap: Map[String, String]): Option[String] = {
    theMap.keysIterator.map(key => {
      var matches: Option[Regex.Match] = RANGE_PATTERN.findFirstMatchIn(key)
      matches.foreach {
        mach =>
          {
            var lowVal = mach.group(1)
            var highVal = mach.group(2)
            if (key >= lowVal && key <= highVal) {
              Some(theMap.get(key))
            }
          }
      }
    })
    None
  }

  def getMapByRange(yKey: String): Option[Map[String, String]] = {
    twoDimMap.keysIterator.map(key => {
      var matches: Option[Regex.Match] = RANGE_PATTERN.findFirstMatchIn(key)
      matches.foreach {
        mach =>
          {
            var lowVal = mach.group(1)
            var highVal = mach.group(2)
            if (yKey >= lowVal && yKey <= highVal) {
              Some(twoDimMap.get(key))
            }
          }
      }
    })
    None
  }

  /**
   * @param yKey
   * @param xKey
   * @return
   */
  def getVal(yKey: String, xKey: String): Option[String] = {
    val m =
      if (yKeyUsesRange) {
        getMapByRange(yKey)
      } else {
        twoDimMap.get(yKey)
      }

    m match {
      case Some(xMap) => {
        if (xKeyUsesRange) {
          getValByRange(xKey, xMap)
        } else {
          xMap.get(xKey)
        }
      }
      case None => None
    }
  }

}