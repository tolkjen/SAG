package system.items

import system.items.ItemType.ItemType

/** Interface for the consumer instance. Consumer is responsible for creating demands */
trait Consumer extends StatisticsCounter {

  /** Returns a request item which should be delivered to the consumer. */
  def requestItem: ItemType

  def deliveredItem: Unit

  /** Set probabilities for each item type to be generated. */
  def setProbabilities(p: Map[ItemType, Double])
}
