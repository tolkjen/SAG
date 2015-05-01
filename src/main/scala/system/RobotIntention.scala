package system

import system.level.Point

trait RobotIntention
case class MoveIntention(destination: Point) extends RobotIntention