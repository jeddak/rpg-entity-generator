package com.treeblossom.gmutil
import java.io.File
import java.io.FileReader
import java.io.FilenameFilter
import java.io.Reader
import java.util.LinkedHashMap
import org.yaml.snakeyaml.Yaml
import scala.collection.mutable.Map
import scala.collection.JavaConverters._
import scala.collection.convert.Wrappers
/**
 * @author jdonald
 *
 */
object TableManager {

  val lookupTables: Map[String, LookupTable] = Map[String, LookupTable]()

  var rootNode: LinkedHashMap[String, Object] = null
  var yamlFileFilter = new FilenameFilter {
    override def accept(dir: File, name: String): Boolean =
      name.endsWith(".yaml")
  }

  /**
   * @param mapsPath
   */
  def loadMaps(mapsPath: String): Unit = {

    var tableDir: File = new File(mapsPath)
    var tableFiles = tableDir.list(yamlFileFilter) //.asScala
    tableFiles.map(f => {
      loadTableFile(mapsPath + "/" + f)
    })
    // do

  }

  /**
   * @param tableFileName
   */
  def loadTableFile(tableFileName: String): Unit = {
    //print("loading " + tableFileName)//DEBUG
    var tableFile = new File(tableFileName)
    var yamlParser: org.yaml.snakeyaml.Yaml = new Yaml
    var yamlFile: Reader = new FileReader(tableFile)
    var rootNode: LinkedHashMap[String, Object] = yamlParser.load(yamlFile)
    var tableNode: LinkedHashMap[String, Object] = rootNode.get("table").asInstanceOf[LinkedHashMap[String, Object]]
    var tableName: String = tableNode.get("name").asInstanceOf[String]
    //println(" ( " + tableName + " )")//DEBUG
    var newLookupTable: LookupTable = new LookupTable(tableName)
    var yLabel: String = tableNode.get("y-axis").asInstanceOf[String]
    newLookupTable.yAxisLabel = yLabel
    var xLabel: String = null
    if (tableNode.containsKey("x-axis")) {
      xLabel = tableNode.get("x-axis").asInstanceOf[String]
      newLookupTable.xAxisLabel = Some(xLabel)
    }

    newLookupTable.xAxisLabel match {
      case Some(xl) => { // 2-dimensional table
        var yentries: LinkedHashMap[String, LinkedHashMap[String, String]] = tableNode.get("entries").asInstanceOf[LinkedHashMap[String, LinkedHashMap[String, String]]]
        var ykeys = yentries.keySet().asScala
        ykeys.map(ykey => {
          var xentries: LinkedHashMap[String, String] = yentries.get(ykey)
          var xkeys = xentries.keySet().asScala
          xkeys.map(xkey => newLookupTable.putVal(ykey, xkey, xentries.get(xkey)))
        })
      }
      case None => { // 1-dimensional table
        var e = tableNode.get("entries")
        var entries: LinkedHashMap[String, String] = tableNode.get("entries").asInstanceOf[LinkedHashMap[String, String]]
        var keys = entries.keySet().asScala
        keys.map(key => newLookupTable.putVal(key, entries.get(key)))

      }
    }

    lookupTables.put(tableName, newLookupTable)
  }
  
  def get(tableName:String):Option[LookupTable] = {
    lookupTables.get(tableName)
  }

//  def main(args: Array[String]): Unit = {
//    loadMaps(args(0))
//  }

}
