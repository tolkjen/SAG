package system

import system.items.{ItemType, Producer, ProducerImpl}
import ItemType.ItemType
import system.level.{Point, LevelMap}
import system.robot.{BringerRobot, Robot}

/** Runs the warehouse simulation.
  *
  * @param level level map describing the warehouse interior.
  */
class SimulationSystemImpl(val level: LevelMap) extends Warehouse {
  private val emptyLevel = level.copy
  private val producer: Producer = new ProducerImpl
  private val robots: Array[Robot] = Array.tabulate(2)(i =>
    new BringerRobot(this, producer, emptyLevel, level.randomEmptyPosition))

  @volatile
  private var simulationStopRequested = false
  private val simulationThread = new Thread(new Runnable {
    override def run(): Unit = {
      val millis = 1000
      while (!simulationStopRequested) {
        for (robot <- robots)
          robot.progress(millis)
        Thread.sleep(millis)
      }
    }
  })

  /** Starts the simulation in a new thread. */
  def start(): Unit = {
    simulationThread.start()
  }

  /** Stops the simulation and blocks the current thread until the simulation finishes. */
  def stop(): Unit = {
    simulationStopRequested = true
    simulationThread.join()
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
}
