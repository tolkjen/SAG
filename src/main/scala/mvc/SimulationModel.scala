package mvc

import system.level.LevelMap
import system.robot.Robot
import system.robot.RobotType.RobotType

/** Model in Model-View-Controller pattern. Subclasses should implement simulation logic. */
trait SimulationModel {
  private var controller: Option[SimulationController] = None

  def registerController(controller: SimulationController) = {
    this.controller = Some(controller)
  }

  /** Start (or resume) the simulation using given options. */
  def start(options: SimulationOptions)

  /** Stop (or pause) the simulation. */
  def stop()

  /** Set the speed at which simulation is played. Value 1.0 means normal, default pace. */
  def setSimulationSpeed(speed: Double)

  /** Model should reset statistics and then tell view to update. */
  def resetStatistics()

  /** Should be called by model when warehouse state changes and view should redraw to reflect it. */
  protected def onWarehouseChanged(levelMap: LevelMap, robots: Array[Robot]): Unit = {
    if(controller.isDefined)
      controller.get.onWarehouseChanged(levelMap, robots)
  }

  protected def onStatisticsChanged(rt: RobotType, averageTime: Long, totalItems: Int): Unit = {
    if(controller.isDefined)
      controller.get.onStatisticsChanged(rt, averageTime, totalItems)
  }
}
