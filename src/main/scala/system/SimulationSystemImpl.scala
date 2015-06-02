package system

import mvc.{SimulationModel, SimulationOptions}
import system.items.ItemType.ItemType
import system.items._
import system.level.{LevelMap, Point}
import system.robot.RobotType.{Bringer, Deliverer, RobotType}
import system.robot.{BringerRobot, DelivererRobot, Robot}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** Runs the warehouse simulation.
  *
  * @param level level map describing the warehouse interior.
  */
class SimulationSystemImpl(val level: LevelMap) extends Warehouse with SimulationModel {
  private val emptyLevel = level.copy
  private val producer: Producer = new ProducerImpl
  private val consumer: Consumer = new ConsumerImpl
  private val robotMap = mutable.HashMap[RobotType, ListBuffer[Robot]](
    Bringer -> ListBuffer.empty[Robot], Deliverer -> ListBuffer.empty[Robot])

  /** Time interval which is passed to robotMap, producer and consumer after each simulation step. */
  private val initialMillis = 1000
  /** Time interval of a simulation step. */
  private var currentMillis = initialMillis

  @volatile
  private var simulationStopRequested = false
  private var simulationThread: Thread = null

  initProducerAndConsumer()

  private def startSimulationThread(): Unit = {
    simulationThread = new Thread(new Runnable {
      override def run(): Unit = {
        while(!simulationStopRequested) {
          producer.progress(initialMillis)
          consumer.progress(initialMillis)
          for (robot <- robots)
            robot.progress(initialMillis)
          onWarehouseChanged(level, robots.toArray)
          Thread.sleep(currentMillis)
        }
      }
    })
    simulationThread.start()
  }

  /** Starts the simulation in a new thread. */
  override def start(options: SimulationOptions): Unit =
  {
    if(simulationThread == null) {
      simulationStopRequested = false
      updateRobots(options.robotCounts)
      producer.setProbabilities(options.producerProbabilities)
      consumer.setProbabilities(options.consumerProbabilities)
      BringerRobot.CommunicationRadius = options.communicationRadius
      DelivererRobot.CommunicationRadius = options.communicationRadius
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
      robot <- robots.toArray
      if (robot.position - p).length <= radius
    } yield robot

  override def set(p: Point, item: ItemType): Unit = {
    level.set(p, item)
  }

  override def get(p: Point): Option[ItemType] =
    level.get(p)

  override def clear(p: Point): Unit =
    level.clear(p)

  /**
   * Set the speed at which the simulation is played. It should not affect the time sense of robots,
   * producer or consumer. Average time measured by producer/consumer should not be affected.
   * @param speed value 1.0 means normal, default speed
   */
  override def setSimulationSpeed(speed: Double): Unit = {
    currentMillis = (initialMillis / speed).toInt
  }

  override def resetStatistics(): Unit = {
    producer.resetStatistics()
    consumer.resetStatistics()
  }

  private def initProducerAndConsumer() = {
    consumer.setStatisticsListener((s: Statistic) => onStatisticsChanged(Deliverer, s.averageTimeMillis, s.totalItems))
    producer.setStatisticsListener((s: Statistic) => onStatisticsChanged(Bringer, s.averageTimeMillis, s.totalItems))
  }

  private def updateRobots(robotCounts: Map[RobotType, Int]) = {
    for((robotType, desiredCount) <- robotCounts) {
      val currentCount: Int = robotMap(robotType).length
      if(desiredCount > currentCount) {
        for(i <- 1 to desiredCount - currentCount) {
          robotMap(robotType) += newRobot(robotType)
        }
      } else if(desiredCount < currentCount) {
        robotMap(robotType) = robotMap(robotType).drop(currentCount - desiredCount)
      }
    }
  }

  private def newRobot(rt: RobotType): Robot = rt match {
    case Deliverer => new DelivererRobot(this, consumer, emptyLevel, level.randomEmptyPosition)
    case Bringer => new BringerRobot(this, producer, emptyLevel, level.randomEmptyPosition)
  }

  private def robots: Iterable[Robot] = robotMap.values.flatten
}
