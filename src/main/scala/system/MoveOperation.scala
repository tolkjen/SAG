package system

import system.level.Point

class MoveOperation(robot: Robot, destination: Point) extends RobotOperation {
  val startPoint = new Point(robot.position)
  var done: Boolean = false

  def progress(dt: Double): Unit = {
    if (destination != robot.position) {
      val diff = destination - robot.position
      val delta = diff / diff.length * Robot.speed * dt
      val oldPosition = robot.position.toIntPoint
      robot.position += delta

      if ((robot.position - startPoint).length >= (destination - startPoint).length) {
        robot.position = destination
        done = true
      }

      if (oldPosition != robot.position.toIntPoint)
        robot.signalPosition()
    } else {
      done = true
    }
  }
}
