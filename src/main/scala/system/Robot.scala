package system

import java.util.concurrent.TimeoutException

import akka.actor.{TypedActor, TypedProps, ActorSystem}
import system.level.{LevelMap, Point}

import scala.util.Random

object Robot {
  val speed = 1.0
  val radius = 5.0
  val system = ActorSystem("warehouse")
  val random = new Random
}

class Robot(var position: Point, warehouse: Warehouse) {
  def this(x: Double, y: Double, warehouse: Warehouse) = this(new Point(x, y), warehouse)

  var actor: Option[RobotService] = None
  var operation: Option[RobotOperation] = None
  val level = warehouse.level.copy

  def progress(dt: Double): Unit = {
    if (actor.isDefined && !operation.isDefined)
      operation = chooseOperation()

    if (operation.isDefined) {
      operation.get.progress(dt)
      if (operation.get.done) {
        actor.get.signalDone()
        operation = chooseOperation()
      }
    }
  }

  def chooseOperation(): Option[RobotOperation] = actor match {
    case Some(act) => act.getIntention match {
      case Some(MoveIntention(dest)) => Some(new MoveOperation(this, dest))
      case _ => None
    }
    case None => None
  }

  def inRadius(that: Robot): Boolean = (that.position - position).length <= Robot.radius

  def signalPosition(): Unit = {
    if (actor.isDefined) {
      val robots = warehouse.receivers ++ warehouse.senders
      val actorsInRadius = for (robot <- robots if robot != this && inRadius(robot) && robot.actor.isDefined)
        yield robot.actor.get
      actor.get.broadcast(actorsInRadius)
      actor.get.position(position.toIntPoint)
    }
  }

  def startActor(): Unit ={
    val name = "robot-" + Robot.random.nextInt(100000)
    actor = Some(TypedActor(Robot.system).typedActorOf(TypedProps[RobotServiceImpl](), name))
    actor.get.setLevelMap(level)
    actor.get.position(position.toIntPoint)
  }

  def stopActor(): Unit = {
    TypedActor(Robot.system).stop(actor.get)
  }
}