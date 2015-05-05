package system

import system.level.{LevelMap, Point}

import scala.concurrent.Future

trait RobotService {
  def getIntention: Option[RobotIntention]

  def signalDone(): Unit

  def setLevelMap(level: LevelMap): Unit

  def position(p: Point): Unit

  def broadcast(actors: Array[RobotService]): Unit

  def getLevelMap: Future[LevelMap]
}
