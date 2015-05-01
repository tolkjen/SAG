package system

trait RobotActor {
  def getIntention: RobotIntention
  def signalDone(): Unit
}
