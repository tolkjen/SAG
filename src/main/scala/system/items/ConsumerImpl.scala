package system.items

import system.items.ItemType._

import scala.util.Random

class ConsumerImpl extends Consumer {

  private val random = new Random()

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
    // TODO: update statistics
  }

  override def resetStatistics(): Unit = {
    // TODO:
  }

  override def setProbabilities(p: Map[ItemType, Double]): Unit = {
    var sum: Double = 0
    p.values.foreach(v => sum += v)
    itemProbabilities = p mapValues { case (v) => v / sum }
    currentItem = randomItem()
  }

}