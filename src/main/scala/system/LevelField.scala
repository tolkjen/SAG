package system

import system.FieldType.FieldType

class LevelField(val fieldType: FieldType) {
  def this() = this(FieldType.Empty)
}
