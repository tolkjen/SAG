package mvc

import system.level.LevelMap
import system.robot.Robot
import system.robot.RobotType._

/** Controller in Model-View-Controller pattern. Passes data between model(simulation system) and view. */
class SimulationController(val model: SimulationModel, val view: SimulationView) {

   model.registerController(this)
   view.registerController(this)

   def onWarehouseChanged(levelMap: LevelMap, robots: Array[Robot]) = view.redraw(levelMap, robots)
   def onStatisticsChanged(rt: RobotType, averageTime: Long, itemCount: Int): Unit = {
     view.updateStatistics(rt, averageTime, itemCount)
   }

   def onStartSimulationButtonClicked(options: SimulationOptions) = {
     model.start(options)
   }

   def onStopSimulationButtonClicked(): Unit = model.stop()
   def onNewSpeedSet(speed: Double): Unit = model.setSimulationSpeed(speed)
   def onResetStatisticsButtonClicked() = model.resetStatistics()
 }
