package system.level

/** Describes the type of the field on each level map. */
object FieldType extends Enumeration {
  type FieldType = Value
  val Empty, Shelf, Producer, Consumer = Value
}
