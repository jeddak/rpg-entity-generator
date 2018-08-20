package com.treeblossom.gmutil
import java.io.FileReader
import java.io.Reader
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.LinkedHashMap

import scala.collection.JavaConverters._
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
import scala.util.matching.Regex

import org.jesperj.MersenneTwister
import org.yaml.snakeyaml.Yaml
import scala.collection.convert.Wrappers
import scala.util.{ Try, Success, Failure }

/**
 * TODO either update the tree with resolved node values OR generate a new tree of fully-resolved nodes.
 */
object EntityGenerator {
  val INDENT: Integer = 2
  val FILL: String = "                                                                                                                 "
  val TOKEN_EVAL: String = "\\%"
  val TOKEN_STARTREF: String = "{"
  val TOKEN_ENDREF: String = "}"

  val EVAL_PATTERN: Regex = """\\%(.+)\\%""".r
  val REF_PATTERN: Regex = """\{[a-zA-Z|_|/| ]+\}""".r
  val DICEROLLSPEC_PATTERN: Regex = """[0-9]+[dD][0-9]+""".r
  val DICEROLLSPEC_DECONSTRUCT_PATTERN: Regex = """([0-9]+)[dD]([0-9]+)""".r
  val TABLELOOKUP_PATTERN: Regex = """\[([^|]+)\|([^|]+)\|*(.+)*\]""".r
  val ALPHA_PATTERN: Regex = """\p{Alpha}""".r
  val toolbox = currentMirror.mkToolBox() // This is actually spinning up a runtime(!), ideal.

  val INDICATOR_KEY_VAL_SEPARATOR = " :"
  val INDICATOR_SEQUENCE_ENTRY = "- "

  var rootNode: LinkedHashMap[String, Object] = null

  val tableManager = TableManager
  tableManager.loadMaps("/home/jdonald/devwork/scala/rpg-entity-generator/tables") //TODO get tables path from args(1)
  /**
   * Walk a parsed yaml-document tree.
   *
   */
  def traverse(node: Object, name: String, level: Int, isInArrayList: Boolean, isFirstInArrayList: Boolean): Unit = {
    var output =
      if (isInArrayList) {
        name + INDICATOR_KEY_VAL_SEPARATOR
      } else {
        FILL.substring(0, level * INDENT) + name + INDICATOR_KEY_VAL_SEPARATOR
      }
    print(output)
    if (node.isInstanceOf[LinkedHashMap[String, Object]]) {
      println()
      var nodeAsMap = node.asInstanceOf[LinkedHashMap[String, Object]]
      var newLevel = level + 1
      var keys = nodeAsMap.keySet().asScala
      keys.map(key => traverse(nodeAsMap.get(key), key, newLevel, false, false))
    } else if (node.isInstanceOf[ArrayList[LinkedHashMap[String, Object]]]) {
      println()
      var newLevel = level + 1
      var nodeAsList = node.asInstanceOf[ArrayList[LinkedHashMap[String, Object]]]
      traverseList(nodeAsList, newLevel)
    } else printNodeValue(node)
  }

  /**
   *
   */
  def printNodeValue(node: Object) = {
    if (node.isInstanceOf[String]) {
      var nodeAsString = node.asInstanceOf[String]
      var fullyMaterializedValue = resolveNodeValue(nodeAsString)
      println(fullyMaterializedValue)
    } else if (node.isInstanceOf[Integer]) {
      var nodeAsInt = node.asInstanceOf[Integer]
      println(nodeAsInt)
    } else if (node.isInstanceOf[Double]) {
      var nodeAsDouble = node.asInstanceOf[Double]
      println(nodeAsDouble)
    } else if (node.isInstanceOf[Boolean]) {
      var nodeAsBool = node.asInstanceOf[Boolean]
      println(nodeAsBool)
    } else if (node.isInstanceOf[Date]) {
      var nodeAsDate = node.asInstanceOf[Date]
      println(nodeAsDate)
    }
  }

  /**
   * Walk an unnamed node
   *
   *
   */
  def traverseAnonymous(node: Object, level: Int): Unit = {

    if (node.isInstanceOf[LinkedHashMap[String, String]]) {
      var nodeAsMap = node.asInstanceOf[LinkedHashMap[String, String]]
      var keys = nodeAsMap.keySet().asScala
      var i: Int = 0
      //var newMap :LinkedHashMap[String,String] = new LinkedHashMap[String,String]
      keys.map(key => {
        var v: Object = nodeAsMap.get(key)
        var resVal: String =
          if (v.isInstanceOf[String]) {
            resolveNodeValue(v.asInstanceOf[String])
          } else {
            v.toString()
          }
        outputStringMapEntry(key, resVal, level, (i == 0))
        //newMap.put(key,resVal)
        //nodeAsMap.put(key,resVal)
        i = i + 1
      })

    } else if (node.isInstanceOf[LinkedHashMap[String, Object]]) {
      var nodeAsMap = node.asInstanceOf[LinkedHashMap[String, Object]]
      var keys = nodeAsMap.keySet().asScala
      keys.map(key => traverse(nodeAsMap.get(key), key, level, false, false))
    } else if (node.isInstanceOf[ArrayList[LinkedHashMap[String, Object]]]) {
      println(" ")
      var newLevel = level + 1
      var nodeAsList = node.asInstanceOf[ArrayList[LinkedHashMap[String, Object]]]
      println(" ")
      traverseList(nodeAsList, newLevel)
    } else printNodeValue(node)
  }

  /**
   *
   */
  def outputStringMapEntry(key: String, value: String, level: Int, isFirst: Boolean) {
    var output =
      if (!isFirst) {
        FILL.substring(0, (level * INDENT))
      } else {
        FILL.substring(0, (level * INDENT) - INDENT) + INDICATOR_SEQUENCE_ENTRY
      }
    println(output + key + INDICATOR_KEY_VAL_SEPARATOR + value)
  }

  /**
   * Iterate over an ArrayList of nodes.
   */
  def traverseList(nodeAsList: ArrayList[LinkedHashMap[String, Object]], level: Int): Unit = {
    for (thisListNode <- (nodeAsList.asScala)) {
      traverseAnonymous(thisListNode, level)
    }
  }

  /**
   * Reduces a node's value to a literal <tt>String</tt> value.
   * Given a <tt>String</tt>, resolves any special embedded tokens
   * replacing their contents with literal character values.
   */
  def resolveNodeValue(strIn: String): String = {
    var trimmedStr = strIn.trim()
    var result = trimmedStr
    var matches: Option[Regex.Match] = EVAL_PATTERN.findFirstMatchIn(result)
    matches.foreach { mach => result = mach.group(1) }
    if (result.length() < trimmedStr.length) {
      result = resolveReferences(result) // not working for template/weight
      result = resolveDiceRollSpecs(result)
      result = resolveTableLookups(result)
      if (result.trim().length > 0) {
        //if result contains alpha characters, don't try to evaluate it mathematically
        ALPHA_PATTERN.findFirstIn(result) match {
          case Some(alphaFound) => result
          case None             => evaluateAsArithmetic(result)
        }

      } else {
        result
      }
    } else {
      result
    }
  }

  /**
   * Given a <tt>String</tt> containing one or more node references,
   * resolves those node references and substitutes in the original <tt>String</tt> the actual resolved value
   * of the referenced node.
   */
  def resolveReferences(strIn: String): String = {
    var refMatches: Regex.MatchIterator = REF_PATTERN.findAllIn(strIn)
    //println("found " + refMatches.length + " references")// DEBUG consumes iterability
    var result = strIn.trim()
    refMatches.foreach { thisMatch: String =>
      {
        var newVal: String = findAndResolve(rootNode, thisMatch)
        result = result.replace(thisMatch, newVal)
      }
    }
    result
  }

  /**
   * Find a node given an address, and resolve its value.
   * The node must be resolvable to a String value.
   */
  def findAndResolve(startNode: LinkedHashMap[String, Object], addr: String): String = {
    var addrStr = addr.replace(TOKEN_STARTREF, "").replace(TOKEN_ENDREF, "")
    var node: Option[Object] = findNode(startNode, addrStr)
    node match {
      case Some(x) => resolveNodeValue(x.asInstanceOf[String])
      case None    => ""
    }
  }

  /**
   * Given a <tt>String</tt> containing one or more dice roll specifications,
   * resolves each dice roll specification within the <tt>String</tt> to the actual resolved value.
   *
   * TODO ensure that any text outside of the dice roll spec is not lost
   */
  def resolveDiceRollSpecs(strIn: String): String = {
    var refMatches: Regex.MatchIterator = DICEROLLSPEC_PATTERN.findAllIn(strIn)
    var result = strIn
    refMatches.foreach { thisMatch => result = result.replace(thisMatch.toString(), resolveDiceRollSpec(thisMatch)) }
    result
  }

  /**
   * Given a dice roll specification, rolls the virtual dice specified and returns the result as a <tt>String</tt>.
   */
  def resolveDiceRollSpec(diceRollStr: String): String = {
    var diceRollParts: Option[Regex.Match] = DICEROLLSPEC_DECONSTRUCT_PATTERN.findFirstMatchIn(diceRollStr)
    var numDice: Int = 0
    var numSides: Int = 0
    diceRollParts.foreach { m => numDice = Integer.parseInt(m.group(1)) }
    diceRollParts.foreach { m => numSides = Integer.parseInt(m.group(2)) }
    var intResult: Int = 0
    for (n <- 1 to numDice) intResult = intResult + (new MersenneTwister().nextInt(numSides - 1)) + 1

    "" + intResult
  }

  /**
   * Given a <tt>String</tt> containing one or more lookup table references,
   * resolves the table lookups and returns the result as a <tt>String</tt>.
   */
  def resolveTableLookups(strIn: String): String = {
    var result = strIn
    var refMatches: Regex.MatchIterator = TABLELOOKUP_PATTERN.findAllIn(strIn)
    refMatches.foreach {
      thisMatch => result = result.replace(thisMatch.toString(), resolveTableLookup(thisMatch))
    }
    result
  }

  /**
   * Look up value from a table.
   * TODO get the regex working
   */
  def resolveTableLookup(strIn: String): String = {
    var result = strIn
    var tableLookupStr: Option[Regex.Match] = TABLELOOKUP_PATTERN.findFirstMatchIn(strIn)
    tableLookupStr match {
      case Some(lookupStr) => {
        var tableName: String = lookupStr.group(1).trim()
        var yParam: String = lookupStr.group(2).trim()
        var xParm: Option[String] = None
        Try(
          if (lookupStr.group(3) != null) {
            var x = lookupStr.group(3)
            xParm = Some(x.trim())

          })
        var table: Option[LookupTable] = tableManager.get(tableName)
        table.foreach(t =>
          xParm match {
            case Some(xParam) => {
              t.getVal(yParam, xParam) match {
                case Some(r) => result = r
                case None    => result = result
              }
            }
            case None => {
              t.getVal(yParam) match {
                case Some(r) => result = r
                case None    => result = result
              }
            }
          })
      }
      case None => result
    }
    result
  }

  /**
   *
   * TODO use a more efficient means of evaluation.
   */
  def evaluateAsArithmetic(strExpr: String): String = {
    val arithmeticResult = toolbox.eval(toolbox.parse(strExpr))
    "" + arithmeticResult
  }

  /**
   * Given an 'address' in the yaml-document tree,
   * returns the node at that location.
   */
  def findNode(node: Object, address: String): Option[Object] = {
    findNode(node, address.split('/'))
  }

  /**
   * Given an 'address' in the yaml-document tree,
   * returns the node at that location.
   * Recursive.
   */
  def findNode(node: Object, targetAddr: Array[String]): Option[Object] = {
    if (node.isInstanceOf[LinkedHashMap[String, Object]]) {
      var nodeAsMap = node.asInstanceOf[LinkedHashMap[String, Object]]
      var restOfAddr: Array[String] = Array()
      if (targetAddr.length > 0) {
        if (targetAddr.length > 1) {
          restOfAddr = Arrays.copyOfRange(targetAddr, 1, targetAddr.length)
        }
        findNode(nodeAsMap.get(targetAddr(0)), restOfAddr)
      } else {
        Some(node) // default
      }
    } else {
      Some(node) // default
    }
  }

  /**
   *
   */
  def main(args: Array[String]): Unit = {

    var templatePath: String = args(0)
    //templatePath = "/home/jdonald/devwork/scala/rpg-entity-generator/char_template.yaml"
    //templatePath = "/home/jdonald/devwork/scala/rpg-entity-generator/hit_locations.yaml"
    //templatePath= "/home/jdonald/devwork/scala/rpg-entity-generator/invoice.yaml"
    // templatePath = "/home/jdonald/devwork/scala/rpg-entity-generator/1d_simple_table.yaml"
    //templatePath = "/home/jdonald/devwork/scala/rpg-entity-generator/2d_simple_table.yaml"

    var yamlParser: org.yaml.snakeyaml.Yaml = new Yaml
    var yamlFile: Reader = new FileReader(templatePath)

    //var templateMap :LinkedHashMap[String, Object] = yaml.load(yamlFile);
    //var template  :ArrayList[LinkedHashMap[String, Object]] = yaml.load(yamlFile);

    rootNode = yamlParser.load(yamlFile);

    //println(t.values())//dumps everything
    //println(t.keySet())
    //var template  : LinkedHashMap[String, Object] =  t.get("template").asInstanceOf[ LinkedHashMap[String, Object]]

    //println(template)
    //var build = template.get("build")
    //println(build)

    //var attr = template.get("attributes")
    //var std_skills = templateMap.get("standard_skills")
    //var pro_skills = templateMap.get("professional_skills")

    //println( findNode(rootNode,"template/stats/SIZ") )
    //println( findNode(rootNode,"template/build") )

    //var weightNode = findNode(rootNode,"template/weight")
    //println(  "weightNode = " + weightNode)

    //println(resolveNodeValue(weightNode.asInstanceOf[String]))

    // println("resolved to " +  resolveNodeValue("\\% ( {template/stats/SIZ} * (5 + ({template/build} * 2)) )  \\%") )

    //println(resolveNodeValue("\\% ( (768  - 34 ) / 33)  + 3D6 \\%"))

    //println(resolveDiceRollSpec("4D6"))

    //println(resolveDiceRollSpecs("4D6 something 2D8+3"))

    //traverse(findNode(rootNode,"template/hit_locations"),"hit_locations",0,false,false)

    traverse(rootNode, "template", 0, false, false)

  }
}