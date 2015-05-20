package ui

import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color._
import scalafx.scene.paint.Paint
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text

object Tile {
  val defaultDimension: Double = 40.0
}

class Tile(val x:Int, val y:Int, paint:Paint, text:String) extends StackPane {

  private val dimension: Double = Tile.defaultDimension

  private val rect = Rectangle(dimension, dimension, paint)
  rect.setStroke(DarkGray)

  private val tileText = new Text("")

  children.addAll(rect, tileText)

  def this(x:Int, y:Int, paint:Paint) {
    this(x, y, paint, "")
  }

  def setFill(paint: Paint) = rect.setFill(paint)

  def setText(text: String) = tileText.setText(text)
}
