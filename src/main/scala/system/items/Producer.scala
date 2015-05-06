package system.items

import system.level.ItemType.ItemType

trait Producer {
  def newItem: ItemType
}
