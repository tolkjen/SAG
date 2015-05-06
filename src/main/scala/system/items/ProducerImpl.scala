package system.items

import system.level.ItemType
import system.level.ItemType.ItemType

class ProducerImpl extends Producer {
  override def newItem: ItemType = ItemType.Blue
}
