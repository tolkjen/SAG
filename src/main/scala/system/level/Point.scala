package system.level

/** Factory object for the Point class. */
object Point {
  def apply(x: Double, y: Double): Point = new Point(x, y)
}

/** Represents a point on a 2-D space. */
class Point(var x: Double = 0.0, var y: Double = 0.0) {
  def this(that: Point) = this(that.x, that.y)
  def +(that: Point): Point = new Point(x + that.x, y + that.y)
  def -(that: Point): Point = new Point(x - that.x, y - that.y)
  def *(m: Double): Point = new Point(x * m, y * m)
  def /(d: Double): Point = new Point(x / d, y / d)
  def ==(that: Point): Boolean = x == that.x && y == that.y
  def !=(that: Point): Boolean = !(this == that)
  def length: Double = Math.sqrt(x*x + y*y)

  /** Returns a copy of this instance with all coordinates rounded to Ints. */
  def toIntPoint: Point = new Point(x.toInt, y.toInt)

  /** X coordinate rounded to Int. */
  def xInt: Int = x.toInt

  /** Y coordinate rounded to Int. */
  def yInt: Int = y.toInt

  override def toString = "("+x+", "+y+")"

  override def equals(o: Any): Boolean = o match {
    case that: Point => this == that
    case _ => false
  }

  override def hashCode = toString.hashCode
}
