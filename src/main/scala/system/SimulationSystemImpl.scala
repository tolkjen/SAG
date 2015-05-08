package system

import system.items.{Producer, ProducerImpl}
import system.level.ItemType.ItemType
import system.level.{Point, LevelMap}
import system.robot.{BringerRobot, Robot}

class SimulationSystemImpl(val level: LevelMap) extends Warehouse {
  private val emptyLevel = level.copy
  private val producer: Producer = new ProducerImpl
  private val robots: Array[Robot] = Array.tabulate(5)(i =>
    new BringerRobot(this, producer, emptyLevel, new Point(2, 0)))

  @volatile
  private var simulationStopRequested = false
  private val simulationThread = new Thread(new Runnable {
    override def run(): Unit = {
      val millis = 1000
      while (!simulationStopRequested) {
        for (robot <- robots)
          robot.progress(millis / 1000.0)
        Thread.sleep(millis)
      }
    }
  })

  def start(): Unit = {
    simulationThread.start()
  }

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
