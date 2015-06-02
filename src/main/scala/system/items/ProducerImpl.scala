package system.items


import java.io.{PrintWriter, BufferedWriter, FileWriter}


import system.items.ItemType._

import scala.util.Random

class ProducerImpl extends Producer {

  private var currentTime: Double = 0
  private val random = new Random()
  private val name = "Producer_" + (10000 + random.nextInt(90000)) + ".txt"
  private val durationQueue = new DurationQueue(5)
  private var counter = 0
  /** Map of item types and probabilities (values from range: [0..1]) that an item of given type will be generated. */
  private var itemProbabilities: Map[ItemType, Double]
    = Map(ItemType.Red -> 1/3.0, ItemType.Green -> 1/3.0, ItemType.Blue -> 1/3.0)

  /** Item waiting to be picked up by BringerRobot. */
  private var currentItem: ItemType = randomItem()

  /** Returns a random item generated using itemProbabilities. */
  override def newItem: ItemType = {
    val item = currentItem
    currentItem = randomItem()

    counter += 1
    durationQueue.add(currentTime)

    val out = new PrintWriter(new BufferedWriter(new FileWriter(name, true)))
    out.println(counter + " " + durationQueue.averageDuration)
    out.close()

    item
  }

  override def progress(dt: Double): Unit = {
    currentTime += dt
    onStatisticsChanged(new Statistic(durationQueue.averageDuration.toLong, counter))
  }

  override def resetStatistics(): Unit = {
    counter = 0
    durationQueue.clear()
    onStatisticsChanged(new Statistic(0, 0))  // tell GUI to update
  }

  override def setProbabilities(p: Map[ItemType, Double]): Unit = {
    var sum: Double = 0
    p.values.foreach(v => sum += v)
    itemProbabilities = p mapValues { case(v) => v/sum }
    currentItem = randomItem()
  }

  private def randomItem(): ItemType = {
    val rand: Double = random.nextDouble()
    var value: Double = 0.0
    itemProbabilities.foreach{ case (item, prob) =>
      value += prob
      if(value >= rand) return item
    }
    itemProbabilities.keys.last
  }
}