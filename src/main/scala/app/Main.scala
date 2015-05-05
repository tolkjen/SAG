package app

import system.Warehouse
import system.level.{Point, LevelMap}

object Main {
  def main(args: Array[String]): Unit = {
    val warehouseSystem = new Warehouse(LevelMap.fromResource("/map1.txt"))
    warehouseSystem.start()
    readLine()
    warehouseSystem.stop()
  }
}
