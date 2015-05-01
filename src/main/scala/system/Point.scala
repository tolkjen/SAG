package system

class Point(var x: Double, var y: Double) {
  def this(that: Point) = this(that.x, that.y)
  def +(that: Point): Point = new Point(x + that.x, y + that.y)
  def -(that: Point): Point = new Point(x - that.x, y - that.y)
  def *(m: Double): Point = new Point(x * m, y * m)
  def /(d: Double): Point = new Point(x / d, y / d)
  def ==(that: Point): Boolean = x == that.x && y == that.y
  def !=(that: Point): Boolean = !(this == that)
  def length: Double = Math.sqrt(x*x + y*y)
  override def toString = "("+x+", "+y+")"
}
