package system

trait RobotIntention
case class MoveIntention(destination: Point) extends RobotIntention