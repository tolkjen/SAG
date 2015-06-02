package ui

import system.robot.RobotType
import system.robot.RobotType._
import ui.Widgets.{LongLabel, SmallButton, TitleLabel}

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.{HBox, VBox}

class RobotPanel extends VBox {

  private val initialRobotCount: Int = 7

  private class SettingSection(text: String, initValue: Int, minValue: Int, maxValue: Int) extends HBox {
    private val valueLabel = new LongLabel(initValue.toString)
    private var removeButton: SmallButton = null
    private val addButton: SmallButton = new SmallButton("+") {
      onAction = handle {
        val current: Int = getValue
        if(current == maxValue - 1) addButton.setDisable(true)
        if(current == minValue) removeButton.setDisable(false)
        if(current < maxValue) valueLabel.setText((current + 1).toString)
      }
    }
    removeButton = new SmallButton("-") {
      onAction = handle {
        val current: Int = getValue
        if(current == minValue + 1) removeButton.setDisable(true)
        if(current == maxValue) addButton.setDisable(false)
        if(current > minValue) valueLabel.setText((current - 1).toString)
      }
    }
    children = Seq(new LongLabel(text), valueLabel, addButton, removeButton)
    alignmentInParent = Pos.Center
    spacing = 20
    padding = Insets(10, 10, 10, 10)

    def setEnabled(value: Boolean) = {
      if(!value || getValue < maxValue)
        addButton.setDisable(!value)
      if(!value || getValue > minValue)
        removeButton.setDisable(!value)
    }

    def getValue: Int = {
      valueLabel.getText.toInt
    }
  }

  private val bringerSection: SettingSection
    = new SettingSection(RobotType.Bringer.toString, initialRobotCount, 0, 30)
  private val delivererSection: SettingSection
    = new SettingSection(RobotType.Deliverer.toString, initialRobotCount, 0, 30)
  private val rangeSection: SettingSection
    = new SettingSection("Communication radius:", 3, 1, 10)

  children = Seq(new TitleLabel("Robots"), bringerSection, delivererSection, rangeSection)
  style = Widgets.borderStyle

  def setEnabled(value: Boolean) = {
    bringerSection.setEnabled(value)
    delivererSection.setEnabled(value)
    rangeSection.setEnabled(value)
  }

  def getRobotCounts: Map[RobotType, Int] = {
    Map(RobotType.Bringer -> bringerSection.getValue, RobotType.Deliverer -> delivererSection.getValue)
  }

  def getCommunicationRadius: Int = {
    rangeSection.getValue
  }
}
