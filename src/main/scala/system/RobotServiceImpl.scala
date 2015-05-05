package system

import system.level.{LevelMap, Point}
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object ServiceState extends Enumeration {
  type ServiceState = Value
  val Walk, Start = Value
}

class RobotServiceImpl extends RobotService {
  implicit val ec = ExecutionContext.Implicits.global

  var level: Option[LevelMap] = None
  var position: Option[Point] = None
  var intentionStack: mutable.Stack[RobotIntention] = mutable.Stack()
  var state = ServiceState.Start

  override def getIntention: Option[RobotIntention] = intentionStack.isEmpty match {
    case false => Some(intentionStack.top)
    case true => None
  }

  override def signalDone(): Unit = state match {
    case ServiceState.Walk =>
      intentionStack.pop()
      if (intentionStack.isEmpty)
        walkToRandomPlace()
  }

  override def setLevelMap(l: LevelMap): Unit = {
    level = Some(l)
  }

  override def broadcast(actors: Array[RobotService]): Unit = {
    val requests = actors map (a => a.getLevelMap) toList
    val request = Future.sequence(requests)
    request onSuccess {
      case levelsReceived =>
        if (levelsReceived.nonEmpty) {
          level match {
            case Some(l) =>
              val levels = levelsReceived ++ List(l)
              level = Some(levels.reduce(LevelMap.merge))
            case None => level = Some(levelsReceived.reduce(LevelMap.merge))
          }
        }
    }
  }

  override def getLevelMap: Future[LevelMap] =
    Future.successful(level.get)

  override def position(p: Point): Unit = {
    position = Some(p)
    if (state == ServiceState.Start) {
      moveToWalkState()
    }
  }

  def walkToRandomPlace(): Unit = {
    if (level.isDefined && position.isDefined) {
      val destination: Point = level.get.randomEmptyPosition(position)
      val path: List[Point] = level.get.getPath(position.get, destination)
      intentionStack.clear()
      for (point <- path) intentionStack.push(new MoveIntention(point))
    }
  }

  def moveToWalkState(): Unit = {
    walkToRandomPlace()
    state = ServiceState.Walk
  }
}
