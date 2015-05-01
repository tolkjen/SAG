package system

import java.io.InputStream

import scala.util.Random

object LevelMap {
  val random: Random = new Random()

  def fromResource(filename: String): LevelMap = {
    val stream: InputStream = getClass.getResourceAsStream(filename)
    val lines: List[String] = scala.io.Source.fromInputStream( stream ).getLines().toList

    if (lines.exists(line => line.length != lines.head.length))
      throw new Exception("The line length doesn't match.")

    val levelMap = new LevelMap(lines.head.length, lines.length)
    levelMap.data = (for (line <- lines) yield {
      (for (ch <- line) yield ch match {
        case '.' => new LevelField
        case '#' => new LevelField(FieldType.Shelf)
        case _ => throw new Exception("Unknown field type.")
      }).toArray
    }).toArray
    levelMap
  }
}

class LevelMap(width: Int, height: Int) {
  private var data: Array[Array[LevelField]] = Array.tabulate(height, width)((y, x) => new LevelField)

  def this() = this(10, 10)

  def randomEmptyPosition(): Point = {
    var p: Point = null
    do {
      p = new Point(LevelMap.random.nextInt(width), LevelMap.random.nextInt(height))
    } while (data(p.y.toInt)(p.x.toInt).fieldType != FieldType.Empty)
    p
  }
}
