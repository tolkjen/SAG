package system.robot

import system.Warehouse
import system.items.Producer
import system.items.ItemType.ItemType
import system.level.{PathNotFoundException, FieldType, LevelMap, Point}
import system.robot.BringerState.BringerState
import system.robot.RobotType.RobotType

import scala.collection.mutable
import scala.util.Random

object BringerRobot {
  // How often (milliseconds) a robot should update it's level map
  private val CommunicationInterval = 1000

  private val ScanDuration = 3000

  // How far the level map update can reach around the robot
  var CommunicationRadius = 5.0

  private val random = new Random()

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
  var itemCarried: Option[ItemType] = None
  val robotType: RobotType = RobotType.Bringer

  private var state: BringerState = BringerState.Pickup
  private var target: Point = _
  private var timeLived = 0.0
  private var lastCommunication = 0.0
  private var scanStartedAt = 0.0
  private var scanInProgress = false
  private val name = BringerRobot.randomName
  private val movementStack: mutable.Stack[Point] = mutable.Stack()
  private var scannedShelves: List[Point] = List[Point]()

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
      if (timeLived - scanStartedAt >= BringerRobot.ScanDuration) {
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

      if (state == BringerState.Bring && level.hasItem(target)) {
//        log("recreates bringing route!")
        createBringMoveStack()
      }
    }
  }

  private def createNewMoveStack(): Unit = state match {
    case BringerState.Pickup =>
      itemCarried = Some(producer.newItem)
      state = BringerState.Bring
//      log("picked up item from " + target)
      createBringMoveStack()
    case BringerState.Bring =>
      warehouse.get(target) match {
        case Some(item) =>
//          log("tried to bring item to the full shelf!")
          level.set(target, item)
          scannedShelves = scannedShelves ++ List(target)
          createBringMoveStack()
        case None =>
//          log("brought item at " + target)
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
      val pathToEmptyShelf = level.path(position, level.findAllEmptyShelves)
      target = pathToEmptyShelf.last
      movementStack.clear()
      movementStack.pushAll(pathToEmptyShelf.take(pathToEmptyShelf.length - 1).reverse)
    } catch {
      case e: PathNotFoundException => createScanStack()
    }
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

  private def createPickupMoveStack(): Unit = {
    try {
      val pathToProducer = level.path(position, level.findAll(FieldType.Producer))
      target = pathToProducer.last
      movementStack.clear()
      movementStack.pushAll(pathToProducer.take(pathToProducer.length - 1).reverse)
    } catch {
      case e: PathNotFoundException =>
        state = BringerState.Paused
        log("can't find Producer - entered paused state")
    }
  }
}
