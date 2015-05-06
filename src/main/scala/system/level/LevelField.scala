package system.level

import system.level.FieldType.FieldType
import system.level.ItemType.ItemType

object LevelField {
  def latest(a: LevelField, b: LevelField): LevelField =
    if (a.lastUpdated >= b.lastUpdated) a else b
}

class LevelField(val fieldType: FieldType, val item: Option[ItemType]) {
  val lastUpdated: Long = System.currentTimeMillis

  def this() = this(FieldType.Empty, None)

  def this(f: FieldType) = this(f, None)

  def this(f: FieldType, i: ItemType) = this(f, Some(i))

  def copy: LevelField = new LevelField(fieldType, item)

  def hasItem: Boolean = item.isDefined
}
