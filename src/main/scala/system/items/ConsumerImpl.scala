package system.items

import java.io.{BufferedWriter, FileWriter, PrintWriter}

import system.items.ItemType._

import scala.util.Random


class ConsumerImpl extends Consumer {

  private val random = new Random()
  private val name = "Consumer_" + (10000 + random.nextInt(90000)) + ".txt"
  private var timeStart = System.currentTimeMillis
  private var timeStop = System.currentTimeMillis
  private var counter = 0
  private var newAverageTime = 0
  /** Map of item types and probabilities (values from range: [0..1]) that an item of given type will be generated. */
  private var itemProbabilities: Map[ItemType, Double]
  = Map(ItemType.Red -> 1 / 3.0, ItemType.Green -> 1 / 3.0, ItemType.Blue -> 1 / 3.0)

  /** Type of item expect to supply by Deliverer Robot */
  private var currentItem: ItemType = randomItem()

  /** Returns a random item generated using itemProbabilities. */
  override def requestItem: ItemType = {
    val item = currentItem
    currentItem = randomItem()
    item
  }

  override def deliveredItem: Unit = {
    counter += 1
    counter % 5 match {
      case 1 =>
        timeStart = System.currentTimeMillis
      case 0 =>
        timeStop = System.currentTimeMillis
        newAverageTime = ((timeStop - timeStart) / 5).toInt
        try {
          val out = new PrintWriter(new BufferedWriter(new FileWriter(name, true)))
          out.println(counter + " " + newAverageTime)
          out.close()
        }
      case _ =>

    }

  }

  private def randomItem(): ItemType = {
    val rand: Double = random.nextDouble()
    var value: Double = 0.0
    itemProbabilities.foreach { case (item, prob) =>
      value += prob
      if (value >= rand) return item
    }
    itemProbabilities.keys.last
  }

  override def progress(dt: Double): Unit = {
    onStatisticsChanged(new Statistic(newAverageTime, counter))
  }

  override def resetStatistics(): Unit = {
    counter = 0
    newAverageTime = 0
    onStatisticsChanged(new Statistic(0, 0))  // tell GUI to update
  }

  override def setProbabilities(p: Map[ItemType, Double]): Unit = {
    var sum: Double = 0
    p.values.foreach(v => sum += v)
    itemProbabilities = p mapValues { case (v) => v / sum }
    currentItem = randomItem()
  }
}