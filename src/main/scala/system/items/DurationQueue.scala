package system.items

import scala.collection.mutable.Queue

class DurationQueue(windowSize: Int) {
  private val queue = new Queue[Double]
  private var previousTimestamp: Double = 0
  private var previousAquired = false

  def add(timestamp: Double): Unit = {
    if (previousAquired) {
      queue += timestamp - previousTimestamp
      if (queue.length > windowSize)
        queue.dequeue()
    }
    previousTimestamp = timestamp
    previousAquired = true
  }

  def clear(): Unit = {
    previousAquired = false
    queue.clear()
  }

  def averageDuration: Double = queue.nonEmpty match {
    case true => queue.sum / queue.length
    case false => 0.0
  }
}
