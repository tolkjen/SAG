package system.robot

/** Describes the state of the [[system.robot.DelivererRobot]]. */
object DelivererState extends Enumeration {
  type DelivererState = Value
  val Free, Scan, Pickup, Deliver, Paused = Value
}
