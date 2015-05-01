package system

import akka.actor.{TypedActor, TypedProps, ActorSystem}

object Robot {
  val speed = 1.0
  val system = ActorSystem("warehouse")
}

class Robot(var position: Point) {
  def this(x: Float, y:Float) = this(new Point(x, y))

  var actor: Option[RobotActor] = None
  var operation: Option[RobotOperation] = None

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

  def chooseOperation(): Option[RobotOperation] = actor.get.getIntention match {
    case MoveIntention(dest) => Some(new MoveOperation(this, dest))
  }

  def startActor(): Unit ={
    actor = Some(TypedActor(Robot.system).typedActorOf(TypedProps[RobotActorImpl]()))
  }

  def stopActor(): Unit ={
    TypedActor(Robot.system).stop(actor.get)
  }
}