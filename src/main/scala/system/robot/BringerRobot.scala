package system.robot

import system.Warehouse
import system.items.ItemType.ItemType
import system.items.Producer
import system.level.{FieldType, LevelMap, PathNotFoundException, Point}
import system.robot.BringerState.BringerState
import system.robot.RobotType._

import scala.collection.immutable.IndexedSeq
import scala.collection.mutable
import scala.util.Random

object BringerRobot {
  // How often (milliseconds) a robot should update it's level map
  private val CommunicationInterval = 1000
  private val random = new Random()
  // How far the level map update can reach around the robot
  var CommunicationRadius = 3.0

  /** Returns a random name for the robot, eg. "R12345" */
  private def randomName: String = "BR" + (10000 + random.nextInt(90000))
}

/** Robot whose job is to pick up an item from the producer and bring it onto an empty shelf.
  *
  * @param warehouse instance implementing the Warehouse trait.
  * @param producer instance implementing the Producer trait.
  * @param level level map representing the initial knowledge about the warehouse level map.
  * @param position initial robot's position on the level map.
  */
class BringerRobot(warehouse: Warehouse, producer: Producer, var level: LevelMap, var position: Point) extends Robot {
  val robotType: RobotType = RobotType.Bringer
  private val scanDelay = 3000
  private val name = BringerRobot.randomName
  private val movementStack: mutable.Stack[Point] = mutable.Stack()
  private val allShelfs: IndexedSeq[Point] = level.findAll(FieldType.Shelf)
  var itemCarried: Option[ItemType] = None
  private var state: BringerState = BringerState.Pickup
  private var target: Point = _
  private var timeLived = 0.0
  private var lastCommunication = 0.0
  private var shelfsScaned: List[Point] = List.empty[Point]
  createPickupMoveStack()

  /** Robot progresses by moving according to the coordinates inside the movement stack. Movement stack contains
    * information about moving from one field to another in order to reach the final destination (eg. Producer, empty
    * shelf). If the stack is empty it means the destination is reached. Once this happens, robot performs some actions
    * (eg. picks up an item from the Producer), modifies it's state variable and calculates the new movement stack (see
    * [[BringerRobot.createNewMoveStack()]]).
    *
    * From time to time robot updates its level map, see: [[BringerRobot.updateLevelMap()]].
    * */
  override def progress(dt: Double): Unit = {
    timeLived += dt
    if (timeLived - lastCommunication > BringerRobot.CommunicationInterval) {
      updateLevelMap()
      lastCommunication = timeLived
    }

    if (movementStack.nonEmpty) {
      val destination = movementStack.top
      val diff = destination - position
      val delta = diff / diff.length * dt / 1000.0

      if (delta.length >= diff.length) {
        position = destination
        movementStack.pop()
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
      log("picked up item from " + target)
      state = BringerState.Bring
      createBringMoveStack()
    case BringerState.Bring =>
      var delay = 0
      log("Scanning shelf at " + target)
      while (delay > scanDelay * 100) delay += 1
      shelfsScaned :+= target
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
      // The path will take the robot from its current position to the nearest empty shelf. Since the path includes the
      // shelf and the robot can't walk onto the shelf, the path's last step is not placed into the movement stack.
      // Instead, the last step becomes the target of the robots action (see [[BringerRobot.createNewMoveStack()]]).
      var shelstoScan = allShelfs diff shelfsScaned
      if (shelstoScan.isEmpty) {
        shelfsScaned = List()
        shelstoScan = allShelfs diff shelfsScaned
      }

      val pathToEmptyShelf = level.path(position, shelstoScan)
      target = pathToEmptyShelf.last
      movementStack.clear()
      movementStack.pushAll(pathToEmptyShelf.take(pathToEmptyShelf.length - 1).reverse)

    } catch {
      // Currently the is no robot to take the items from the shelves to the Consumer so eventually the warehouse will
      // run out of space. When such happens the robot will go into the Paused state.
      case e: PathNotFoundException =>
        state = BringerState.Paused
        log("entered Paused state")
    }
  }

  private def createPickupMoveStack(): Unit = {
    try {
      val pathToProducer = level.path(position, level.findAll(FieldType.Producer))
      target = pathToProducer.last
      movementStack.clear()
      movementStack.pushAll(pathToProducer.take(pathToProducer.length - 1).reverse)
    } catch {
      case e: PathNotFoundException =>
        state = BringerState.Paused
        log("entered Paused state")
    }
  }
}