package system.robot

import system.Warehouse
import system.items.Producer
import system.level.ItemType.ItemType
import system.level.{PathNotFoundException, FieldType, LevelMap, Point}
import system.robot.BringerState.BringerState

import scala.collection.mutable
import scala.util.Random

private object BringerRobot {
  val CommunicationInterval = 1000
  val CommunicationRadius = 3.0
  val random = new Random()

  def randomName: String = "R" + (10000 + random.nextInt(90000))
}

class BringerRobot(warehouse: Warehouse, producer: Producer, var level: LevelMap, var position: Point) extends Robot {
  var itemCarried: Option[ItemType] = None
  var state: BringerState = BringerState.Pickup
  var target: Point = _
  var timeLived = 0.0
  var lastCommunication = 0.0
  val name = BringerRobot.randomName
  val moveStack: mutable.Stack[Point] = mutable.Stack()

  createPickupMoveStack()

  override def progress(dt: Double): Unit = {
    timeLived += dt
    if (timeLived - lastCommunication > BringerRobot.CommunicationInterval) {
      updateLevelMap()
      lastCommunication = timeLived
    }

    if (moveStack.nonEmpty) {
      val destination = moveStack.top
      val diff = destination - position
      val delta = diff / diff.length * dt / 1000.0

      if (delta.length >= diff.length) {
        position = destination
        moveStack.pop()
        log("is at " + position)
      } else {
        position += delta
      }
    } else {
      createNewMoveStack()
    }
  }

  private def log(s: String): Unit = {
    println(name + ": " + s)
  }

  private def updateLevelMap(): Unit = {
    val robotsNearby = warehouse.nearbyRobots(position, BringerRobot.CommunicationRadius)
    val levels = robotsNearby map (robot => robot.level)
    level = levels.reduce(LevelMap.merge)

    if (state == BringerState.Bring && level.hasItem(target)) {
      log("recreates bringing route!")
      createBringMoveStack()
    }
  }

  private def createNewMoveStack(): Unit = state match {
    case BringerState.Pickup =>
      itemCarried = Some(producer.newItem)
      state = BringerState.Bring
      log("picked up item from " + target)
      createBringMoveStack()
    case BringerState.Bring =>
      warehouse.get(target) match {
        case Some(item) =>
          log("tried to bring item to the full shelf!")
          level.set(target, item)
          createBringMoveStack()
        case None =>
          log("brought item at " + target)
          level.set(target, itemCarried.get)
          warehouse.set(target, itemCarried.get)
          itemCarried = None
          state = BringerState.Pickup
          createPickupMoveStack()
      }
    case BringerState.Paused =>
  }
  
  private def createBringMoveStack(): Unit = {
    try {
      val pathToEmptyShelf = level.path(position, level.positionEmptyShelves())
      target = pathToEmptyShelf.last
      moveStack.clear()
      moveStack.pushAll(pathToEmptyShelf.take(pathToEmptyShelf.length - 1).reverse)
    } catch {
      case e: PathNotFoundException =>
        state = BringerState.Paused
        log("entered Paused state")
    }
  }

  private def createPickupMoveStack(): Unit = {
    try {
      val pathToProducer = level.path(position, level.position(FieldType.Producer))
      target = pathToProducer.last
      moveStack.clear()
      moveStack.pushAll(pathToProducer.take(pathToProducer.length - 1).reverse)
    } catch {
      case e: PathNotFoundException =>
        state = BringerState.Paused
        log("entered Paused state")
    }
  }
}
