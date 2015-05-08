package system.robot

import system.level.ItemType.ItemType
import system.level.{LevelMap, Point}

trait Robot {
  var position: Point

  def progress(dt: Double): Unit

  def itemCarried: Option[ItemType]

  def level: LevelMap
}
