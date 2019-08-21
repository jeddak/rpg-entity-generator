package com.treeblossom.gmutil

import scala.collection.JavaConverters._
import scala.collection.convert.Wrappers
import scala.util.matching.Regex
import scala.collection.mutable.Map

/**
 * A type of dictionary
 * allowing lookups by key or by <i>range of keys</i>.
 * May be one-dimensional (like a HashTable), 
 * or two-dimensional (like a Map of Maps), where the keys are analogous to Cartesian x.y coordinates.
 * @author jdonald
 *
 */
class LookupTable(val name: String) {

  val RANGE_PATTERN: Regex = """\[(.+)\-(.+)\]""".r
  var yAxisLabel: String = ""
  var xAxisLabel: Option[String] = None
  var oneDimMap: Map[String, String] = null
  var twoDimMap: Map[String, Map[String, String]] = null
  var yKeyUsesRange: Boolean = false
  var xKeyUsesRange: Boolean = false

  /**
   * Returns <code>true</code> if the <i>keyStr</i> uses the Range Syntax,
   * and <code>false</code> if otherwise.
   */
  def keyUsesRangeSyntax(keyStr: String): Boolean = {
    var result: Boolean = false
    var matches: Option[Regex.Match] = RANGE_PATTERN.findFirstMatchIn(keyStr)
    var lowVal: String = null
    var highVal: String = null
    matches.foreach { mach =>
      {
        lowVal = mach.group(1)
        highVal = mach.group(2)
        //print("lowVal = " + lowVal)
        //println(", highVal = " + highVal)
        result = true
      }
    }
    result
  }

  /**
   * Store a value in a one-dimensional LookupTable.
   * @param yKey
   * @param value
   */
  def putVal(yKey: String, value: String): Unit = {
    if (oneDimMap == null) {
      oneDimMap = Map()
    }
    if (!yKeyUsesRange) {
      yKeyUsesRange = keyUsesRangeSyntax(yKey)
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
    if (!yKeyUsesRange) {
      yKeyUsesRange = keyUsesRangeSyntax(yKey)
    }
    
    if (!xKeyUsesRange) {
      xKeyUsesRange = keyUsesRangeSyntax(xKey)
    }

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
    var result :Option[String] = None
    for ((kee :String, valu :String) <- oneDimMap) {
      var matches: Option[Regex.Match] = RANGE_PATTERN.findFirstMatchIn(kee)
      matches.foreach {
        mach =>
          {
            var lowVal = mach.group(1).trim()
            var highVal = mach.group(2).trim()
            if (yKey >= lowVal && yKey <= highVal) {
              result = Some(valu)
            }
          }
      }
    }
    result
  }

  /**
   * Retrieve a table value where the lookup key is a range of values.
   */
  def getValByRange(key: String, theMap: Map[String, String]): Option[String] = {
    theMap.keysIterator.map(key => {
      var matches: Option[Regex.Match] = RANGE_PATTERN.findFirstMatchIn(key)
      matches.foreach {
        mach =>
          {
            var lowVal = mach.group(1).trim()
            var highVal = mach.group(2).trim()
            if (key >= lowVal && key <= highVal) {
              Some(theMap.get(key))
            }
          }
      }
    })
    None
  }

  /**
   * Retrieve a Map from a two-dimensional LookupTable.
   */
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
   * Retrieve a value from a two-dimensional LookupTable.
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