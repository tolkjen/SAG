package system.level

import java.io.InputStream

import system.level.FieldType.FieldType
import system.level.ItemType.ItemType

import scala.collection.immutable.IndexedSeq
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
        case 'C' => new LevelField(FieldType.Consumer)
        case 'P' => new LevelField(FieldType.Producer)
        case _ => throw new Exception("Unknown field type.")
      }).toArray
    }).toArray
    levelMap
  }

  def merge(a: LevelMap, b: LevelMap): LevelMap = {
    val newMap = new LevelMap(a.width, a.height)
    for (row <- 0 until a.height) {
      for (col <- 0 until a.width) {
        newMap.data(row)(col) = LevelField.latest(a.data(row)(col), b.data(row)(col)).copy
      }
    }
    newMap
  }
}

class LevelMap(val width: Int, val height: Int) {
  private var data: Array[Array[LevelField]] = Array.tabulate(height, width)((y, x) => new LevelField)

  def this() = this(10, 10)

  def randomEmptyPosition(forbidden: Option[Point] = None): Point = {
    var p: Point = null
    do {
      p = new Point(LevelMap.random.nextInt(width), LevelMap.random.nextInt(height))
    } while (data(p.y.toInt)(p.x.toInt).fieldType != FieldType.Empty || (forbidden.isDefined && forbidden.get == p))
    p
  }

  def copy: LevelMap = {
    val mapCopy = new LevelMap(width, height)
    for (row <- 0 until height) {
      for (col <- 0 until width) {
        mapCopy.data(row)(col) = data(row)(col).copy
      }
    }
    mapCopy
  }

  private def neighbors(p: Point): List[Point] =
    for {
      point <- List(new Point(p.x-1, p.y), new Point(p.x, p.y-1), new Point(p.x+1, p.y), new Point(p.x, p.y+1))
      if point.x >= 0 && point.y >= 0 && point.x < width && point.y < height
    } yield point

  private def emptyNeighbors(p: Point): List[Point] =
    for {
      point <- neighbors(p)
      if data(point.y.toInt)(point.x.toInt).fieldType == FieldType.Empty
    } yield point

  private def unvisitedEmptyNeighbors(map: Array[Array[Int]], p: Point): List[Point] =
    for (point <- emptyNeighbors(p) if map(point.y.toInt)(point.x.toInt) == 0)
      yield point

  private def walkingPath(map: Array[Array[Int]], p: Point): List[Point] = {
    val currentStep = map(p.y.toInt)(p.x.toInt)
    val previousPoint = emptyNeighbors(p).filter(point => map(point.y.toInt)(point.x.toInt) == currentStep - 1).head
    if (map(previousPoint.y.toInt)(previousPoint.x.toInt) == 1)
      List(p)
    else
      walkingPath(map, previousPoint) ++ List(p)
  }

  private def unblockedPath(start: Point, ends: Seq[Point]): List[Point] = {
    if (!ends.contains(start)) {
      val stepMap: Array[Array[Int]] = Array.tabulate(height, width)((x, y) => 0)
      stepMap(start.y.toInt)(start.x.toInt) = 1

      var currentPoints: List[Point] = List(start)
      while (currentPoints.nonEmpty) {
        val newPoints: List[Point] = currentPoints flatMap (point => unvisitedEmptyNeighbors(stepMap, point))
        for (point <- newPoints) {
          val currentPoint: Point = currentPoints.head
          stepMap(point.y.toInt)(point.x.toInt) = stepMap(currentPoint.y.toInt)(currentPoint.x.toInt) + 1
          if (ends.contains(point)) {
            return walkingPath(stepMap, point)
          }
        }
        currentPoints = newPoints
      }

      throw new PathNotFoundException
    } else {
      List[Point]()
    }
  }
  
  def position(fType: FieldType): IndexedSeq[Point] =
    for {
      y <- 0 until height
      x <- 0 until width
      if data(y)(x).fieldType == fType
    } yield Point(x, y)

  def positionEmptyShelves(): IndexedSeq[Point] =
    for {
      y <- 0 until height
      x <- 0 until width
      if data(y)(x).fieldType == FieldType.Shelf
      if !data(y)(x).hasItem
    } yield Point(x, y)

  def path(start: Point, ends: IndexedSeq[Point]): List[Point] = {
    val endsNeighbors = ends flatMap (p => emptyNeighbors(p))
    val path = unblockedPath(start, endsNeighbors)
    val destination = path.nonEmpty match {
      case true => neighbors(path.last).toSet.intersect(ends.toSet).head
      case false => neighbors(start).toSet.intersect(ends.toSet).head
    }
    path.toList ++ List(destination)
  }

  def get(p: Point): Option[ItemType] =
    data(p.yInt)(p.xInt).item

  def set(p: Point, item: ItemType): Unit = {
    data(p.yInt)(p.xInt) = new LevelField(data(p.yInt)(p.xInt).fieldType, item)
  }
}
