package system.items

import system.items.ItemType.ItemType

class ProducerImpl extends Producer {

  /** Returns an item of a fixed type (for simplicity). */
  override def newItem: ItemType = ItemType.Blue
}
