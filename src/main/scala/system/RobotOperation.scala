package system

trait RobotOperation {
  def progress(dt: Double): Unit
  def done: Boolean
}
