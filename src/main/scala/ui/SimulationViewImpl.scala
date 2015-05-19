package ui

import mvc.SimulationView
import system.items.ItemType.ItemType
import system.level.LevelMap
import system.robot.Robot
import system.robot.RobotType.RobotType

import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.paint.Color._

class SimulationViewImpl(levelMap: LevelMap) extends PrimaryStage with SimulationView {

  title = "SAG"
  private val warehouseGrid = new WarehouseGrid(levelMap.width, levelMap.height)

  private val robotPanel = new RobotPanel
  private val itemPanel = new ItemPanel
  private val statisticPanel = new StatisticPanel(onResetStatisticsButtonClicked)
  private val simulationPanel = new SimulationControlPanel

  simulationPanel.setSimulationControlListener(new SimulationControlListener {

    override def startSimulationButtonClicked(): Unit = {
      val pProbs: Option[Map[ItemType, Double]] = itemPanel.getProducerProbabilities
      if(!pProbs.isDefined) return
      val cProbs: Option[Map[ItemType, Double]] = itemPanel.getConsumerProbabilities
      if(!cProbs.isDefined) return

      simulationPanel.enableStopButton()
      val counts: Map[RobotType, Int] = robotPanel.getRobotCounts
      robotPanel.setEnabled(false)
      itemPanel.setEnabled(false)
      onStartSimulationButtonClicked(pProbs.get, cProbs.get, counts)
    }

    override def newSpeedSet(speed: Double): Unit = {
      onNewSpeedSet(speed)
    }

    override def stopSimulationButtonClicked(): Unit = {
      onStopSimulationButtonClicked()
      robotPanel.setEnabled(true)
      itemPanel.setEnabled(true)
      simulationPanel.enableStartButton()
    }
  })

  scene = new Scene(
    Math.max(700, warehouseGrid.widthInPixels + 500),
    Math.max(650, warehouseGrid.heightInPixels + 300))
  {
    minWidth = width.get()
    minHeight = height.get()

    fill = White
    root = new GridPane {
      padding = Insets(20)
      hgap = 20
      vgap = 20

      add(warehouseGrid, 0, 0)
      GridPane.setRowSpan(warehouseGrid, 2)
      add(simulationPanel, 1, 0)
      add(itemPanel, 1, 1)
      add(robotPanel, 1, 2)
      add(statisticPanel, 0, 2)
    }
  }

  override def redraw(levelMap: LevelMap, robots: Array[Robot]): Unit = {
    Platform.runLater(new Runnable {
      override def run(): Unit = warehouseGrid.redraw(levelMap, robots)
    })
  }

  override def updateStatistics(rt: RobotType, averageTime: Long, itemCount: Int): Unit = {
    Platform.runLater(new Runnable {
      override def run(): Unit = statisticPanel.update(rt, averageTime, itemCount)
    })
  }
}