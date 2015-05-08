package system.level

import java.io.InputStream

import system.items.ItemType
import system.level.FieldType.FieldType
import ItemType.ItemType

import scala.collection.immutable.IndexedSeq
import scala.util.Random

object LevelMap {
  private val random: Random = new Random()

  /** Loads a level map from a text file.
    *
    * Text file must contain only a set of characters. The file must have the same number of characters in each row.
    * Characters translate:
    *  '.' into an empty field
    *  '#' into a shelf
    *  'C' into a Consumer space
    *  'P' into a Producer space
    *
    * @param filename the name of the text file relative to /main/resources directory.
    * @return LevelMap instance
    */
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

  /** Produces a new level map which has the latest field information from both given level maps. */
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

/** Contains information about the warehouse level grid.
  *
  * By default the whole level is empty (no shelves/producers/consumers).
  *
  * @param width width of the level (in grid units, eg. 10)
  * @param height height of the level grid (eg. 5)
  */
class LevelMap(val width: Int, val height: Int) {
  private var data: Array[Array[LevelField]] = Array.tabulate(height, width)((y, x) => new LevelField)

  def this() = this(10, 10)

  /** Returns the coordinates of an empty field, randomly chosen. */
  def randomEmptyPosition: Point = {
    var p: Point = null
    do {
      p = new Point(LevelMap.random.nextInt(width), LevelMap.random.nextInt(height))
    } while (data(p.y.toInt)(p.x.toInt).fieldType != FieldType.Empty)
    p
  }

  /** Returns a deep copy of this instance. */
  def copy: LevelMap = {
    val mapCopy = new LevelMap(width, height)
    for (row <- 0 until height) {
      for (col <- 0 until width) {
        mapCopy.data(row)(col) = data(row)(col).copy
      }
    }
    mapCopy
  }

  /** Returns a collection of coordinates of all fields of a given type.
    *
    * @param fType the chosen field type
    */
  def findAll(fType: FieldType): IndexedSeq[Point] =
    for {
      y <- 0 until height
      x <- 0 until width
      if data(y)(x).fieldType == fType
    } yield Point(x, y)

  /** Returns a collection of coordinates of all shelves which don't have any items in them. */
  def findAllEmptyShelves: IndexedSeq[Point] =
    for {
      y <- 0 until height
      x <- 0 until width
      if data(y)(x).fieldType == FieldType.Shelf
      if !data(y)(x).hasItem
    } yield Point(x, y)

  /** Returns the shortest path form the start point (exclusive) to any of the end points (inclusive). 
    * 
    * @param start the start point
    * @param endpoints collection of end points
    */
  def path(start: Point, endpoints: IndexedSeq[Point]): List[Point] = {
    val endsNeighbors = endpoints flatMap (p => emptyNeighbors(p))
    val path = unblockedPath(start, endsNeighbors)
    val destination = path.nonEmpty match {
      case true => neighbors(path.last).toSet.intersect(endpoints.toSet).head
      case false => neighbors(start).toSet.intersect(endpoints.toSet).head
    }
    path.toList ++ List(destination)
  }

  /** Returns the item at the given coordinates on the level map. */
  def get(p: Point): Option[ItemType] =
    data(p.yInt)(p.xInt).item

  /** Sets the item of the level field at the given coordinates. */
  def set(p: Point, item: ItemType): Unit = {
    data(p.yInt)(p.xInt) = new LevelField(data(p.yInt)(p.xInt).fieldType, item)
  }

  /** Returns true if a field at the given coordinates has an item, false otherwise. */
  def hasItem(p: Point): Boolean =
    data(p.yInt)(p.xInt).hasItem

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
}
