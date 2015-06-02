package system.items

trait StatisticsCounter {

  private var statsListener: Option[(Statistic) => Unit] = None

  /** Notifies the counter that some time has passed so it can update statistics. */
  def progress(dt: Double): Unit

  def resetStatistics(): Unit

  def setStatisticsListener(l: (Statistic) => Unit): Unit = {
    statsListener = Some(l)
  }

  protected def onStatisticsChanged(statistic: Statistic): Unit = {
    if(statsListener.isDefined) statsListener.get.apply(statistic)
  }
}
