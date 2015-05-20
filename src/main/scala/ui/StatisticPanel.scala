package ui

import system.robot.RobotType
import system.robot.RobotType.RobotType
import ui.Widgets.{BigButton, TitleLabel}

import scalafx.Includes._
import scalafx.scene.control.Label
import scalafx.scene.layout.GridPane

class StatisticPanel(onResetStatisticsListener: () => Unit) extends GridPane {

  style = Widgets.borderStyle

  private class CustomLabel(text: String) extends Label(text) {
    minWidth = 120
    minHeight = 40
  }

  private val avgTimeBringer = new CustomLabel("-")
  private val avgTimeDeliverer = new CustomLabel("-")

  private val totalItemsBringer = new CustomLabel("0")
  private val totalItemsDeliverer = new CustomLabel("0")

  private val title: TitleLabel = new TitleLabel("Statistics")
  add(title, 0, 0)
  GridPane.setColumnSpan(title, 3)

  add(new CustomLabel("Average\ntime [ms]:"), 0, 2)
  add(new CustomLabel("Items:"), 0, 3)
  add(new CustomLabel("Producer\n -> shelf:"), 1, 1)
  add(new CustomLabel("Shelf\n -> consumer:"), 2, 1)

  add(avgTimeBringer, 1, 2)
  add(avgTimeDeliverer, 2, 2)
  add(totalItemsBringer, 1, 3)
  add(totalItemsDeliverer, 2, 3)

  add(new BigButton("Reset") {
    onAction = handle {
      onResetStatisticsListener()
    }
  }, 2, 4)

  def update(rt: RobotType, averageTime: Long, totalItems: Int): Unit = {
    rt match {
      case RobotType.Bringer =>
        avgTimeBringer.setText(averageTime.toString)
        totalItemsBringer.setText(totalItems.toString)
      case RobotType.Deliverer =>
        avgTimeDeliverer.setText(averageTime.toString) // df.format(averageTime)
        totalItemsDeliverer.setText(totalItems.toString)
    }
  }
}
