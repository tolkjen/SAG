package system

import system.items.ItemType
import ItemType.ItemType
import system.level.Point
import system.robot.Robot

/** Used for interfacing with Robots. */
trait Warehouse {

  /** Returns all the Robots in the radius of the given coordinates.
    *
    * @param p search coordinates
    * @param radius search radius
    */
  def nearbyRobots(p: Point, radius: Double): Array[Robot]

  /** Returns the item type on the true warehouse level map (if any). */
  def get(p: Point): Option[ItemType]

  /** Sets the item on the field of the true warehouse level map.
    *
    * @param p Coordinates of the level field
    * @param item Item type
    */
  def set(p: Point, item: ItemType): Unit
}
