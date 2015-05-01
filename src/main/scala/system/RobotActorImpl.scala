package system

class RobotActorImpl extends RobotActor {
  def getIntention: RobotIntention = new MoveIntention(new Point(0, 0))
  def signalDone() {}
}
