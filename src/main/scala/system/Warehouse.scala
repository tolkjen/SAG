package system

import system.level.ItemType.ItemType
import system.level.Point
import system.robot.Robot

trait Warehouse {
  def nearbyRobots(p: Point, radius: Double): Array[Robot]

  def get(p: Point): Option[ItemType]

  def set(p: Point, item: ItemType): Unit
}
