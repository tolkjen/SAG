package app

import system.SimulationSystemImpl
import system.level.LevelMap

/** Object containing app's entry point. */
object Main {

  /** Application entry point.
    *
    * Starts a simulation using a map loaded from /main/resources directory. Simulation is performed in a secondary
    * thread and can be stopped anytime by pressing the return key.
    */
  def main(args: Array[String]): Unit = {
    val warehouseSystem = new SimulationSystemImpl(LevelMap.fromResource("/map1.txt"))
    warehouseSystem.start()
    readLine()
    warehouseSystem.stop()
  }
}
