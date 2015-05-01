package system

import system.level.LevelMap

import scala.concurrent.Future

trait RobotService {
  def getIntention: RobotIntention

  def signalDone(): Unit

  def setLevelMap(level: LevelMap): Unit

  def broadcast(actors: Array[RobotService]): Unit

  def getLevelMap: Future[LevelMap]
}
