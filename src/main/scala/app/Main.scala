package app

import system.{FieldType, LevelMap, Warehouse}

object Main {
  def main(args: Array[String]): Unit = {
    val warehouseSystem = new Warehouse(LevelMap.fromResource("/map1.txt"))
    warehouseSystem.start()
    readLine()
    warehouseSystem.stop()
  }
}
