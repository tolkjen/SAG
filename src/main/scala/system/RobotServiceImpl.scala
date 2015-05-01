package system

import system.level.{LevelMap, Point}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class RobotServiceImpl extends RobotService {
  implicit val ec = ExecutionContext.Implicits.global

  var level: Option[LevelMap] = None

  override def getIntention: RobotIntention = new MoveIntention(new Point(0, 0))

  override def signalDone() {}

  override def setLevelMap(l: LevelMap): Unit = {
    level = Some(l)
  }

  override def broadcast(actors: Array[RobotService]): Unit = {
    val requests = actors map (a => a.getLevelMap) toList
    val request = Future.sequence(requests)
    request onSuccess {
      case levels => {
        level = Some(levels.reduce(LevelMap.merge))
      }
    }
  }

  override def getLevelMap: Future[LevelMap] = Future.successful(level.get)
}
