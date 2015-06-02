package system.items

import ItemType.ItemType

/** Interface for the producer instance. Producer is responsible for creating new items in the warehouse. */
trait Producer extends StatisticsCounter {

  /** Returns a new item which should be delivered onto a shelf. */
  def newItem: ItemType

  /** Set probabilities for each item type to be generated. */
  def setProbabilities(p: Map[ItemType, Double])
}
