package ui

import system.items.ItemType
import system.items.ItemType._
import system.level.FieldType._
import system.level.{FieldType, LevelMap, Point}
import system.robot.Robot

import scala.collection.mutable.ArrayBuffer
import scalafx.geometry.Insets
import scalafx.scene.layout.GridPane
import scalafx.scene.paint.Color.{Blue, Green, LightGray, Red, _}
import scalafx.scene.paint.Paint

/** Grid view that visualises warehouse current state */
class WarehouseGrid(tilesInRow: Int, tilesInCol: Int) extends GridPane {

  val widthInPixels = tilesInRow * Tile.defaultDimension.toInt
  val heightInPixels = tilesInCol * Tile.defaultDimension.toInt

  private val grid: ArrayBuffer[Tile]
    = ArrayBuffer.tabulate(tilesInRow * tilesInCol)(i => createEmptyTile(i % tilesInRow, i / tilesInRow))

  padding = Insets(20)
  children = {
    for (tile <- grid) yield {
      GridPane.setColumnIndex(tile, tile.x)
      GridPane.setRowIndex(tile, tile.y)
      tile
    }
  }

//  def clear(): Unit = {
//    for(x <- 0 to (tilesInRow - 1)) {
//      for(y <- 0 to (tilesInCol - 1)) {
//        drawEmptyField(getTile(x, y))
//      }
//    }
//  }

  def redraw(levelMap: LevelMap, robots: Array[Robot]) = {
    for(x <- 0 to (levelMap.width - 1)) {
      for(y <- 0 to (levelMap.height - 1)) {
        val point: Point = new Point(x, y)
        val fieldType: FieldType = levelMap.getFieldType(point)
        val tile: Tile = getTile(x, y)

        fieldType match {
          case FieldType.Producer => drawProducer(tile, levelMap.get(point))
          case FieldType.Consumer => drawConsumer(tile, levelMap.get(point))
          case FieldType.Shelf => drawShelf(tile, levelMap.get(point))
          case FieldType.Empty => drawEmptyField(tile)
          case _ => throw new Exception("Unknown field type")
        }
      }
    }
    drawRobots(robots)
  }

  private def createEmptyTile(x: Int, y: Int): Tile = {
    new Tile(x, y, White, "")
  }

  private def drawRobots(robots: Array[Robot]): Unit = {
    for (r <- robots) {
      val p: Point = r.position
      val tile: Tile = getTile(p.xInt, p.yInt)
      drawItem(tile, r.itemCarried, LightGray)
      tile.setText("B")
    }
  }

  private def drawItem(tile: Tile, maybeItem: Option[ItemType], altColor: Paint): Unit = {
    if(maybeItem.isDefined) maybeItem.get match {
      case ItemType.Blue => tile.setFill(Blue)
      case ItemType.Red => tile.setFill(Red)
      case ItemType.Green => tile.setFill(Green)
    } else tile.setFill(altColor)
  }

  private def drawShelf(tile: Tile, maybeItem: Option[ItemType]) = {
    drawItem(tile, maybeItem, SandyBrown)
  }

  private def drawProducer(tile: Tile, maybeItem: Option[ItemType]) = {
    drawItem(tile, maybeItem, Yellow)
    tile.setText("P")
  }

  private def drawConsumer(tile: Tile, maybeItem: Option[ItemType]) = {
    drawItem(tile, maybeItem, Yellow)
    tile.setText("C")
  }

  private def drawEmptyField(tile: Tile): Unit = {
    tile.setFill(White)
    tile.setText("")
  }

  private def getTile(x:Int, y:Int): Tile = {
    grid(y * tilesInRow + x)
  }
}
