package system.robot

import system.Warehouse
import system.items.Consumer
import system.items.ItemType.ItemType
import system.level.{FieldType, LevelMap, PathNotFoundException, Point}
import system.robot.DelivererState.DelivererState
import system.robot.RobotType.RobotType

import scala.collection.immutable.IndexedSeq
import scala.collection.mutable
import scala.util.Random

private object DelivererRobot {
  // How often (milliseconds) a robot should update it's level map
  val CommunicationInterval = 1000

  // How far the level map update can reach around the robot
  val CommunicationRadius = 3.0

  private val random = new Random()

  /** Returns a random name for the robot, eg. "R12345" */
  def randomName: String = "DR" + (10000 + random.nextInt(90000))
}

class DelivererRobot(warehouse: Warehouse, consumer: Consumer, var level: LevelMap, var position: Point) extends Robot {

  val robotType: RobotType = RobotType.Deliverer
  private val scanDelay = 3000
  private val name = DelivererRobot.randomName
  private val movementStack: mutable.Stack[Point] = mutable.Stack()
  private val allShelfs: IndexedSeq[Point] = level.findAll(FieldType.Shelf)
  var itemCarried: Option[ItemType] = None
  private var state: DelivererState = DelivererState.Free
  private var itemRequested: Option[ItemType] = None
  private var target: Point = _
  private var timeLived = 0.0
  private var lastCommunication = 0.0
  private var shelfsScaned: List[Point] = List.empty[Point]
  private var itemonShelfs: mutable.HashMap[Point, ItemType] = mutable.HashMap[Point, ItemType]()
  private var consumerTagret: Point = _


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

  private def updateLevelMap(): Unit = {
    val robotsNearby = warehouse.nearbyRobots(position, DelivererRobot.CommunicationRadius)
    val levels = robotsNearby map (robot => robot.level)
    level = levels.reduce(LevelMap.merge)

    if (state == DelivererState.Deliver && level.hasItem(target)) {
      log("recreates delivering route!")
      createDeliverMoveStack()
    }
  }

  private def itemsPositions(item: ItemType): IndexedSeq[Point] = {
    var positions = IndexedSeq[Point]()
    itemonShelfs.foreach { case (k, v) => if (item == v) positions :+= k
    }
    positions
  }

  private def createNewMoveStack(): Unit = state match {
    case DelivererState.Free =>
      itemRequested = Some(consumer.requestItem)
      log("Requst from the Consumer " + itemRequested.get)

      if (itemsPositions(itemRequested.get).nonEmpty) {
        state = DelivererState.Pickup
        createPickupMoveStack()
      }
      else {
        state = DelivererState.Scan
        log("Request item from " + target)
        createScanMoveStack()
      }

    case DelivererState.Scan =>
      log("Scan shelf at " + target)
      warehouse.get(target) match {
        case Some(item) =>
          itemonShelfs.put(target, warehouse.get(target).get)
        case None =>
      }

      log("Requst from the Consumer: " + itemRequested.get)

      if (itemsPositions(itemRequested.get).nonEmpty) {
        state = DelivererState.Pickup
        createPickupMoveStack()
      }
      else
        createScanMoveStack()
    case DelivererState.Pickup =>
      if (warehouse.get(target).getOrElse(None) == itemRequested.get) {
        itemonShelfs.-=(target)
        itemCarried = warehouse.get(target)
        state = DelivererState.Deliver
        warehouse.clear(target)
        log("picked up item from " + target)
        createDeliverMoveStack()
      }
      else {
        log("Tried pickup wrong item or shelf is empty!")
        itemonShelfs.-=(target)
        state = DelivererState.Scan
        createScanMoveStack()
      }

    case DelivererState.Deliver => {
      itemRequested = None
      itemCarried = None

      state = DelivererState.Free
      log("Item delivered to consumer " + target)
    }
    case DelivererState.Paused =>
  }

  private def createScanMoveStack(): Unit = {
    try {

      var shelstoscan = allShelfs diff shelfsScaned
      if (shelstoscan.isEmpty) {
        shelfsScaned = List()
        shelstoscan = allShelfs diff shelfsScaned
      }
      // The path wille take the robot from its current position to the nearest shelf to scan
      val pathToShelf = level.path(position, shelstoscan)
      target = pathToShelf.last
      shelfsScaned :+= target
      movementStack.clear()
      movementStack.pushAll(pathToShelf.take(pathToShelf.length - 1).reverse)
    }
    catch {
      // Currently the is no robot to take the items from the shelves to the Consumer so eventually the warehouse will
      // run out of space. When such happens the robot will go into the Paused state.
      case e: PathNotFoundException =>
        state = DelivererState.Paused
        log("entered Paused state")
    }
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
      val pathToShelf = level.path(position, itemsPositions(itemRequested.get))
      target = pathToShelf.last
      movementStack.clear()
      movementStack.pushAll(pathToShelf.take(pathToShelf.length - 1).reverse)
    } catch {
      case e: PathNotFoundException =>
        state = DelivererState.Paused
        log("entered Paused state")
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

  private def log(s: String): Unit = {
    println(name + ": " + s)
  }
}
