package mvc

import system.items.ItemType.ItemType
import system.level.LevelMap
import system.robot.Robot
import system.robot.RobotType._

class SimulationController(val model: SimulationModel, val view: SimulationView) {

   model.registerController(this)
   view.registerController(this)

   def onWarehouseChanged(levelMap: LevelMap, robots: Array[Robot]) = view.redraw(levelMap, robots)
   def onStatisticsChanged(rt: RobotType, averageTime: Long, itemCount: Int): Unit = {
     view.updateStatistics(rt, averageTime, itemCount)
   }

   def onStartSimulationButtonClicked(producerProbabilities: Map[ItemType, Double],
                                      consumerProbabilities: Map[ItemType, Double],
                                      robotCounts: Map[RobotType, Int]) = {
     model.start(producerProbabilities, consumerProbabilities, robotCounts)
   }

   def onStopSimulationButtonClicked(): Unit = model.stop()
   def onNewSpeedSet(speed: Double): Unit = model.setSimulationSpeed(speed)
   def onResetStatisticsButtonClicked() = model.resetStatistics()
 }
