package system.items

import system.items.ItemType.ItemType

/** Interface for the consumer instance. Consumer is responsible for creating demands */
trait Consumer {

  /** Returns a request item which should be delivered to the consumer. */
  def requestItem: ItemType

  /** Notifies the consumer that some time has passed so it can update statistics. */
  def progress(dt: Double): Unit

  /** Set probabilities for each item type to be generated. */
  def setProbabilities(p: Map[ItemType, Double])
}
