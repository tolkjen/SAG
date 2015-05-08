package system.robot

/** Describes the state of the [[system.robot.BringerRobot]]. */
object BringerState extends Enumeration {
  type BringerState = Value
  val Pickup, Bring, Paused = Value
}
