package system.robot

import system.Warehouse
import system.items.Consumer
import system.items.ItemType.ItemType
import system.level.{PathNotFoundException, FieldType, LevelMap, Point}
import system.robot.DelivererState._
import system.robot.RobotType.RobotType

import scala.collection.immutable.IndexedSeq
import scala.collection.mutable
import scala.util.Random

object DelivererRobot {
  // How often (milliseconds) a robot should update it's level map
  private val CommunicationInterval = 1000

  private val ScanDuration = 3000

  // How far the level map update can reach around the robot
  var CommunicationRadius = 3.0

  private val random = new Random()

  /** Returns a random name for the robot, eg. "R12345" */
  private def randomName: String = "DR" + (10000 + random.nextInt(90000))
}

/** Robot whose job is to pick up an item from shelf and bring it to consumer.
  *
  * @param warehouse instance implementing the Warehouse trait.
  * @param consumer instance implementing the Producer trait.
  * @param level level map representing the initial knowledge about the warehouse level map.
  * @param position initial robot's position on the level map.
  */
class DelivererRobot(warehouse: Warehouse, consumer: Consumer, var level: LevelMap, var position: Point) extends Robot {

  val robotType: RobotType = RobotType.Deliverer
  var itemCarried: Option[ItemType] = None

  private val name = DelivererRobot.randomName
  private val movementStack: mutable.Stack[Point] = mutable.Stack()
  private var state: DelivererState = DelivererState.Free
  private var itemRequested: Option[ItemType] = None
  private var target: Point = _
  private var timeLived = 0.0
  private var lastCommunication = 0.0
  private var consumerTagret: Point = _

  private var scanStartedAt = 0.0
  private var scanInProgress = false
  private var scannedShelves: List[Point] = List[Point]()

  createFreeMoveStack()

  /** Robot progresses by moving according to the coordinates inside the movement stack. Movement stack contains
    * information about moving from one field to another in order to reach the final destination (eg. Producer, empty
    * shelf). If the stack is empty it means the destination is reached. Once this happens, robot performs some actions
    * (eg. picks up an item from the Producer), modifies it's state variable and calculates the new movement stack (see
    * [[DelivererRobot.createNewMoveStack()]]).
    *
    * From time to time robot updates its level map, see: [[DelivererRobot.updateLevelMap()]].
    * */
  override def progress(dt: Double): Unit = {
    timeLived += dt
    if (timeLived - lastCommunication > DelivererRobot.CommunicationInterval) {
      updateLevelMap()
      lastCommunication = timeLived
    }

    if (movementStack.nonEmpty) {
      val destination = movementStack.top
      // If robot's destination is the same as its position it means that it needs to spend some time on scanning.
      // Because of this it's easy to implement scanning - you basically repeat points in the movement stack. Each
      // repeated point means scanning.
      if (destination != position)
        progressRobotMovement(dt, destination)
      else
        progressScanning()
    } else {
      createNewMoveStack()
    }
  }

  private def progressScanning(): Unit = {
    if (!scanInProgress) {
//      log("is scanning")
      scanInProgress = true
      scanStartedAt = timeLived
    } else {
      if (timeLived - scanStartedAt >= DelivererRobot.ScanDuration) {
        scanInProgress = false
        movementStack.pop()
      }
    }
  }

  private def progressRobotMovement(dt: Double, destination: Point): Unit = {
    val diff = destination - position
    val delta = diff / diff.length * dt / 1000.0
    if (delta.length >= diff.length) {
      position = destination
      movementStack.pop()
//      log("is at " + position)
    } else {
      position += delta
    }
  }

  private def log(s: String): Unit = {
    println(name + ": " + s)
  }

  private def updateLevelMap(): Unit = {
    val robotsNearby = warehouse.nearbyRobots(position, BringerRobot.CommunicationRadius)
    if (robotsNearby.length > 1) {
      val levels = robotsNearby map (robot => robot.level)
      level = levels.reduce(LevelMap.merge)
      if (state == DelivererState.Pickup) {
        if (!level.hasItem(target) || level.get(target).get != itemRequested.get) {
//          log("recreates picking route!")
          createPickupMoveStack()
        }
      }
    }
  }

  private def createNewMoveStack(): Unit = state match {
    case DelivererState.Free =>
      itemRequested = Some(consumer.requestItem)
//      log("Requst from the Consumer " + itemRequested.get)
      state = DelivererState.Pickup
      createPickupMoveStack()
    case DelivererState.Pickup =>
      warehouse.get(target) match {
        case Some(item) =>
          if (item == itemRequested.get)
          {
            itemCarried = warehouse.get(target)
            state = DelivererState.Deliver
            level.clear(target)
            warehouse.clear(target)
//            log("picked up item from " + target)
            createDeliverMoveStack()
          }
          else
          {
            scannedShelves = scannedShelves ++ List(target)
//            log("Tried pickup wrong item")
            level.set(target, item)
            createPickupMoveStack()
          }
        case None =>
          scannedShelves = scannedShelves ++ List(target)
//          log("Tried pickup item from empty shelf!")
          level.clear(target)
          createPickupMoveStack()
      }

    case DelivererState.Deliver =>
      consumer.deliveredItem
      itemRequested = None
      itemCarried = None
      state = DelivererState.Free
//      log("Item delivered to consumer " + target)

    case DelivererState.Paused =>
  }


  private def createScanStack(): Unit = {
    val allShelves = level.findAll(FieldType.Shelf)

    scannedShelves = scannedShelves ++ List(target)
    val unscannedShelves = allShelves diff scannedShelves
    val shelvesToScan = unscannedShelves.nonEmpty match {
      case true => unscannedShelves
      case false =>
        scannedShelves = List(target)
        allShelves
    }

    val pathToShelf = level.path(position, shelvesToScan)
    val longerPath = pathToShelf.length > 1 match {
      case true =>
        val lastStep = pathToShelf(pathToShelf.length - 2)
        pathToShelf.take(pathToShelf.length - 2) ++ List(lastStep, lastStep, pathToShelf.last)
      case false =>
        List(position, pathToShelf.last)
    }

    target = longerPath.last
    movementStack.clear()
    movementStack.pushAll(longerPath.take(longerPath.length - 1).reverse)
  }

  private def createDeliverMoveStack(): Unit = {
    try {
      // The path will take the robot from its current position to the consumer
      val pathToConsumer = level.path(position, IndexedSeq(consumerTagret))
      target = pathToConsumer.last
      movementStack.clear()
      movementStack.pushAll(pathToConsumer.take(pathToConsumer.length - 1).reverse)
    } catch {
      // Currently the is no robot to take the items from the shelves to the Consumer so eventually the warehouse will
      // run out of space. When such happens the robot will go into the Paused state.
      case e: PathNotFoundException =>
        state = DelivererState.Paused
        log("entered Paused state")
    }
  }

  private def createPickupMoveStack(): Unit = {
    try {
      //The path will take the robot to the nearest shelf with requested item
      val pathToShelf = level.path(position, level.findAllRequestedItems(itemRequested))
      target = pathToShelf.last
      movementStack.clear()
      movementStack.pushAll(pathToShelf.take(pathToShelf.length - 1).reverse)
    } catch {
      case e: PathNotFoundException => createScanStack()
    }
  }


  private def createFreeMoveStack(): Unit = {
    try {
      //The path will take the robot to the nearest consumer
      val pathToConsumer = level.path(position, level.findAll(FieldType.Consumer))
      consumerTagret = pathToConsumer.last
      target = pathToConsumer.last
      movementStack.clear()
      movementStack.pushAll(pathToConsumer.take(pathToConsumer.length - 1).reverse)
    } catch {
      case e: PathNotFoundException =>
        state = DelivererState.Paused
        log("entered Paused state")
    }
  }

}
