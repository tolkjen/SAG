package ui

import java.text.DecimalFormat

import ui.Widgets.{BigButton, SmallButton, TitleLabel}

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{HBox, VBox}

trait SimulationControlListener {
  def startSimulationButtonClicked()
  def stopSimulationButtonClicked()
  def newSpeedSet(speed: Double)
}

class SimulationControlPanel extends VBox {
  private var listener: Option[SimulationControlListener] = None

  private var simulationSpeed: Double = 5.0
  private val speedStep: Double = 0.5
  private val maxSpeedAllowed: Double = 100.0
  private val minSpeedAllowed: Double = speedStep

  private val df: DecimalFormat = new DecimalFormat("0.0")

  private var stopButton: Button = null

  private val startButton: Button = new BigButton("Start") {
    onAction = handle {
      if(listener.isDefined)
        listener.get.startSimulationButtonClicked()
    }
  }

  stopButton = new BigButton("Stop") {
    onAction = handle {
      if(listener.isDefined)
        listener.get.stopSimulationButtonClicked()
    }
  }
  stopButton.setDisable(true)

  private val speedLabel = new Label("Simulation speed:")
  private val currentSpeedLabel = new Label(simulationSpeed.toString)

  private val increaseSpeed = new SmallButton("+") {
    onAction = handle {
      simulationSpeed = Math.min(simulationSpeed + speedStep, maxSpeedAllowed)
      currentSpeedLabel.setText(df.format(simulationSpeed))
      if(listener.isDefined)
        listener.get.newSpeedSet(simulationSpeed)
    }
  }

  private val decreaseSpeed = new SmallButton("-") {
    onAction = handle {
      simulationSpeed = Math.max(simulationSpeed - speedStep, minSpeedAllowed)
      currentSpeedLabel.setText(df.format(simulationSpeed))
      if(listener.isDefined)
        listener.get.newSpeedSet(simulationSpeed)
    }
  }

  children = Seq(
    new TitleLabel("Simulation"),
    new HBox {
      alignmentInParent = Pos.Center
      spacing = 20
      children = Seq(startButton, stopButton)
    },
    new HBox {
      spacing = 20
      children = Seq(
        speedLabel,
        currentSpeedLabel,
        increaseSpeed,
        decreaseSpeed
      )
    }
  )
  style = Widgets.borderStyle
  alignmentInParent = Pos.Center
  spacing = 20
  padding = Insets(20)

  def setSimulationControlListener(l: SimulationControlListener): Unit = {
    listener = Some(l)
  }

  def enableStopButton() = {
    startButton.setDisable(true)
    stopButton.setDisable(false)
  }

  def enableStartButton() = {
    startButton.setDisable(false)
    stopButton.setDisable(true)
  }
}
