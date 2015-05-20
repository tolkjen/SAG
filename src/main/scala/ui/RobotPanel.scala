package ui

import system.robot.RobotType
import system.robot.RobotType._
import ui.Widgets.{LongLabel, SmallButton, TitleLabel}

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, VBox}

class RobotPanel extends VBox {

  private val initialRobotCount: Int = 2

  private class RobotSection(robotType: RobotType, count: Int) extends HBox {
    private val countLabel: Label = new LongLabel(count.toString)

    private var removeButton: SmallButton = null
    private val addButton: SmallButton = new SmallButton("+") {
      onAction = handle {
        val current: Int = countLabel.getText.toInt
        if(current == 0) removeButton.setDisable(false)
        countLabel.setText((current + 1).toString)
      }
    }
    removeButton = new SmallButton("-") {
      onAction = handle {
        val current: Int = countLabel.getText.toInt
        if(current == 1) removeButton.setDisable(true)
        if(current > 0) countLabel.setText((current - 1).toString)
      }
    }
    children = Seq(new LongLabel(robotType.toString), countLabel, addButton, removeButton)
    alignmentInParent = Pos.Center
    spacing = 20
    padding = Insets(30, 10, 10, 10)

    def setEnabled(value: Boolean) = {
      addButton.setDisable(!value)
      if(!value || countLabel.getText.toInt > 0)
        removeButton.setDisable(!value)
    }

    def getCount: Int = {
      countLabel.getText.toInt
    }
  }

  private val bringerSection: RobotSection = new RobotSection(RobotType.Bringer, initialRobotCount)
  private val delivererSection: RobotSection = new RobotSection(RobotType.Deliverer, initialRobotCount)

  children = Seq(new TitleLabel("Robots"), bringerSection, delivererSection)
  style = Widgets.borderStyle

  def setEnabled(value: Boolean) = {
    bringerSection.setEnabled(value)
    delivererSection.setEnabled(value)
  }

  def getRobotCounts: Map[RobotType, Int] = {
    Map(RobotType.Bringer -> bringerSection.getCount, RobotType.Deliverer -> delivererSection.getCount)
  }
}
