package system.robot

import system.items.ItemType.ItemType
import system.level.{LevelMap, Point}
import system.robot.RobotType.RobotType

/** Interface for robot implementations. */
trait Robot {

  /** Robot position on the level map. */
  var position: Point

  /** Makes robot do some progress with its current job (eg. move).
    *
    * @param dt Time which has passed since the previous progress call.
    */
  def progress(dt: Double): Unit

  /** The item carried by the robot. */
  def itemCarried: Option[ItemType]

  /** The robot's local knowledge about the true warehouse level map. */
  def level: LevelMap

  /** Robot Type {Deliverer, Bringer} **/
  def robotType: RobotType
}