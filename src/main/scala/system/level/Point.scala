package system.level

object Point {
  def apply(x: Double, y: Double): Point = new Point(x, y)
}

class Point(var x: Double = 0.0, var y: Double = 0.0) {
  def this(that: Point) = this(that.x, that.y)
  def +(that: Point): Point = new Point(x + that.x, y + that.y)
  def -(that: Point): Point = new Point(x - that.x, y - that.y)
  def *(m: Double): Point = new Point(x * m, y * m)
  def /(d: Double): Point = new Point(x / d, y / d)
  def ==(that: Point): Boolean = x == that.x && y == that.y
  def !=(that: Point): Boolean = !(this == that)
  def length: Double = Math.sqrt(x*x + y*y)
  def toIntPoint: Point = new Point(x.toInt, y.toInt)
  def xInt: Int = x.toInt
  def yInt: Int = y.toInt

  override def toString = "("+x+", "+y+")"

  override def equals(o: Any): Boolean = o match {
    case that: Point => this == that
    case _ => false
  }

  override def hashCode = toString.hashCode
}
