package ui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.text.{Font, FontWeight}

object Widgets {

  val borderStyle = "" +
    "-fx-background-color: white;" +
    "-fx-border-color: black;" +
    "-fx-border-width: 1;" +
    "-fx-border-radius: 6;" +
    "-fx-padding: 6;"

  class TitleLabel(text: String) extends Label(text) {
    font = Font.font(null, FontWeight.Bold, 14)
    padding = Insets(0, 10, 10, 0)
  }

  class SmallButton(text: String) extends Button(text) {
    minWidth = 40
    minHeight = 25
  }

  class BigButton(text: String) extends Button(text) {
    minWidth = 100
    minHeight = 30
  }

  class LongLabel(text: String) extends Label(text) {
    alignmentInParent = Pos.Center
    minWidth = 60
  }
}
