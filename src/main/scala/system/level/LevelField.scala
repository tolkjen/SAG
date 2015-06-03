package system.level

import system.items.ItemType
import system.level.FieldType.FieldType
import ItemType.ItemType

object LevelField {

  /** Returns the latest of the two level fields.
    *
    * @param a First level field to be compared
    * @param b Second level field to be compared
    * @return The latest field
    */
  def latest(a: LevelField, b: LevelField): LevelField =
    if (a.createdAt >= b.createdAt) a else b
}

/** Describes each field in the level map.
  *
  * @param fieldType type of the field
  * @param item item stored in that field (only for shelves)
  */
class LevelField(val fieldType: FieldType, val item: Option[ItemType], val createdAt: Long) {

  def this(f: FieldType, i: Option[ItemType]) = this(f, i, System.currentTimeMillis)

  def this(f: FieldType, i: ItemType) = this(f, Some(i))

  def this(f: FieldType) = this(f, None)

  def this() = this(FieldType.Empty, None)

  // Creates a copy of this instance
  def copy: LevelField = new LevelField(fieldType, item, createdAt)

  // Returns true if the field contains an item, false otherwise.
  def hasItem: Boolean = item.isDefined

  // Returns the type of item */
  def typeItem: Option[ItemType] =
      item
}
