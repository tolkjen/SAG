package system.items

import ItemType.ItemType

/** Interface for the producer instance. Producer is responsible for creating new items in the warehouse. */
trait Producer {

  /** Returns a new item which should be delivered onto a shelf. */
  def newItem: ItemType

  def progress(dt: Double): Unit

  def setProbabilities(p: Map[ItemType, Double])
}
