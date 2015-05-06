package app

import system.SimulationSystemImpl
import system.level.{FieldType, Point, LevelMap}

object Main {
  def main(args: Array[String]): Unit = {
    val warehouseSystem = new SimulationSystemImpl(LevelMap.fromResource("/map1.txt"))
    warehouseSystem.start()
    readLine()
    warehouseSystem.stop()
  }
}
