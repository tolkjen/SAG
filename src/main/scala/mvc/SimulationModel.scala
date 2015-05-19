package mvc

import system.items.ItemType._
import system.level.LevelMap
import system.robot.Robot
import system.robot.RobotType.RobotType

trait SimulationModel {
  private var controller: Option[SimulationController] = None

  def registerController(controller: SimulationController) = {
    this.controller = Some(controller)
  }

  def start(producerProbabilities: Map[ItemType, Double],
                      consumerProbabilities: Map[ItemType, Double],
                      robotCounts: Map[RobotType, Int])
  def stop()
  def setSimulationSpeed(speed: Double)
  def resetStatistics()

  protected def onWarehouseChanged(levelMap: LevelMap, robots: Array[Robot]): Unit = {
    if(controller.isDefined)
      controller.get.onWarehouseChanged(levelMap, robots)
  }

  protected def onStatisticsChanged(rt: RobotType, averageTime: Long, totalItems: Int): Unit = {
    if(controller.isDefined)
      controller.get.onStatisticsChanged(rt, averageTime, totalItems)
  }
}
