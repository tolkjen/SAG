package system

import mvc.{SimulationModel, SimulationOptions}
import system.items.ItemType.ItemType
import system.items.{Producer, ProducerImpl, Consumer, ConsumerImpl}
import system.level.{LevelMap, Point}
import system.robot.{BringerRobot, DelivererRobot, Robot, RobotType}

/** Runs the warehouse simulation.
  *
  * @param level level map describing the warehouse interior.
  */
class SimulationSystemImpl(val level: LevelMap) extends Warehouse with SimulationModel {
  private val emptyLevel = level.copy
  private val producer: Producer = new ProducerImpl
  private val consumer: Consumer = new ConsumerImpl
  private val robots: Array[Robot] = Array(
    new DelivererRobot(this, consumer, emptyLevel, level.randomEmptyPosition),
    new BringerRobot(this, producer, emptyLevel, level.randomEmptyPosition))

  /** Time interval which is passed to robots, producer and consumer after each simulation step. */
  private val initialMillis = 1000
  /** Time interval of a simulation step. */
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
  override def start(options: SimulationOptions): Unit =
  {
    if(simulationThread == null) {
      simulationStopRequested = false

      //val robotCounts: Map[RobotType, Int] = options.robotCounts
      // TODO: add robots to map -> robotCounts contains info how many robots of each type user wants

      producer.setProbabilities(options.producerProbabilities)
      //consumer.setProbabilities(options.consumerProbabilities)
      // TODO: set probabilities of every item in consumer (use consumerProbabilities)

      BringerRobot.CommunicationRadius = options.communicationRadius
      // TODO: set communication radius for Deliverer robots

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
    // TODO: reset statistics in Producer and Consumer
    onStatisticsChanged(RobotType.Bringer, 0, 0)
    onStatisticsChanged(RobotType.Deliverer, 0, 0)
  }
}
