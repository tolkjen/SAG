package level

import system.level._

import org.scalatest._

class LevelMapTest extends FlatSpec with Matchers {

  "A LevelMap" should "be loaded with fromResource method" in {
    val level = LevelMap.fromResource("/map.txt")
  }

  it should "return the correct shortest path to the destination fields" in {
    val level = LevelMap.fromResource("/map.txt")
    val path = level.path(Point(0, 0), level.position(FieldType.Producer))
    path should be (List(Point(1, 0), Point(2, 0), Point(3, 0)))
  }

  it should "return a list of all empty shelves" in {
    val level = LevelMap.fromResource("/map.txt")
    level.set(Point(1, 1), ItemType.Blue)
    level.positionEmptyShelves().toList should be (List(Point(1, 2), Point(1, 3), Point(1, 4)))
  }

  it should "return the shortest path to empty shelf" in {
    val level = LevelMap.fromResource("/map.txt")
    level.set(Point(1, 1), ItemType.Blue)
    val path = level.path(Point(2, 1), level.positionEmptyShelves())
    path should be (List(Point(2, 2), Point(1, 2)))
  }
}
