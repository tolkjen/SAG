package system

import mvc.SimulationModel
import system.items.{ItemType, Producer, ProducerImpl}
import ItemType.ItemType
import system.level.{Point, LevelMap}
import system.robot.RobotType._
import system.robot.{RobotType, BringerRobot, Robot}

/** Runs the warehouse simulation.
  *
  * @param level level map describing the warehouse interior.
  */
class SimulationSystemImpl(val level: LevelMap) extends Warehouse with SimulationModel {
  private val emptyLevel = level.copy
  private val producer: Producer = new ProducerImpl
  private val robots: Array[Robot] = Array.tabulate(2)(i =>
    new BringerRobot(this, producer, emptyLevel, level.randomEmptyPosition))

  private val initialMillis = 1000
  private var currentMillis = initialMillis

  @volatile
  private var simulationStopRequested = false
  private var simulationThread: Thread = null

  private def startSimulationThread(): Unit = {
    simulationThread = new Thread(new Runnable {
      override def run(): Unit = {
        while(!simulationStopRequested) {
          producer.progress(initialMillis)
          for (robot <- robots)
            robot.progress(initialMillis)
          onWarehouseChanged(level, robots)
          Thread.sleep(currentMillis)
        }
      }
    })
    simulationThread.start()
  }

  /** Starts the simulation in a new thread. */
  override def start(producerProbabilities: Map[ItemType, Double],
                     consumerProbabilities: Map[ItemType, Double],
                     robotCounts: Map[RobotType, Int]): Unit =
  {
    if(simulationThread == null) {
      simulationStopRequested = false
      // TODO: add robots to map -> robotCounts contains info how many robots of each type user wants
      producer.setProbabilities(producerProbabilities)
      // TODO: set probabilities of every item in consumer (use consumerProbabilities)
      startSimulationThread()
    }
  }

  /** Stops the simulation and blocks the current thread until the simulation finishes. */
  override def stop(): Unit = {
    if(simulationThread != null) {
      simulationStopRequested = true
      simulationThread.join()
      simulationThread = null
    }
  }

  override def nearbyRobots(p: Point, radius: Double): Array[Robot] =
    for {
      robot <- robots
      if (robot.position - p).length <= radius
    } yield robot

  override def set(p: Point, item: ItemType): Unit = {
    level.set(p, item)
  }

  override def get(p: Point): Option[ItemType] =
    level.get(p)

  override def setSimulationSpeed(speed: Double): Unit = {
    currentMillis = (initialMillis / speed).toInt
  }

  override def resetStatistics(): Unit = {
    // TODO: reset statistics in Producer and Consumer
    onStatisticsChanged(RobotType.Bringer, 0, 0)
    onStatisticsChanged(RobotType.Deliverer, 0, 0)
  }
}
