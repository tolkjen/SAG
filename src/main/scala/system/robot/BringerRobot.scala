package system.robot

import system.Warehouse
import system.items.Producer
import system.level.ItemType.ItemType
import system.level.{PathNotFoundException, FieldType, LevelMap, Point}
import system.robot.BringerState.BringerState

import scala.collection.mutable

class BringerRobot(warehouse: Warehouse, producer: Producer, level: LevelMap, var position: Point) extends Robot {
  var itemCarried: Option[ItemType] = None
  var state: BringerState = BringerState.Pickup
  var target: Point = _
  val moveStack: mutable.Stack[Point] = mutable.Stack()

  createPickupMoveStack()

  override def progress(dt: Double): Unit = {
    if (moveStack.nonEmpty) {
      val destination = moveStack.top
      val diff = destination - position
      val delta = diff / diff.length * dt

      if (delta.length >= diff.length) {
        position = destination
        moveStack.pop()
      } else {
        position += delta
        println(position)
      }
    } else {
      createNewMoveStack()
    }
  }

  private def createNewMoveStack(): Unit = state match {
    case BringerState.Pickup =>
      itemCarried = Some(producer.newItem)
      state = BringerState.Bring
      createBringMoveStack()
    case BringerState.Bring =>
      warehouse.get(target) match {
        case Some(item) =>
          level.set(target, item)
          createBringMoveStack()
        case None =>
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
      case e: PathNotFoundException => state = BringerState.Paused
    }
  }

  private def createPickupMoveStack(): Unit = {
    try {
      val pathToProducer = level.path(position, level.position(FieldType.Producer))
      target = pathToProducer.last
      moveStack.clear()
      moveStack.pushAll(pathToProducer.take(pathToProducer.length - 1).reverse)
    } catch {
      case e: PathNotFoundException => state = BringerState.Paused
    }
  }
}
