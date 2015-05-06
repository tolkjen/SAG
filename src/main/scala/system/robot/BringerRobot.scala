package system.robot

import system.Warehouse
import system.items.Producer
import system.level.ItemType.ItemType
import system.level.{LevelMap, Point}

class BringerRobot(warehouse: Warehouse, producer: Producer, level: LevelMap) extends Robot {
  var itemCarried: Option[ItemType] = None
  var position: Point = _

  override def progress(dt: Double): Unit = {

  }
}
