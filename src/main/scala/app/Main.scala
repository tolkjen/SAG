package app

import mvc.SimulationController
import system.SimulationSystemImpl
import system.level.LevelMap
import ui.SimulationViewImpl

import scalafx.application.JFXApp

/**
 * Object containing app's entry point.
 * Starts a simulation using a map loaded from /main/resources directory.
 * Simulation is performed in a secondary thread.
 */
object Main extends JFXApp {

  private val map: LevelMap = LevelMap.fromResource("/map1.txt")

  private val view = new SimulationViewImpl(map)
  stage = view

  private val model = new SimulationSystemImpl(map)
  new SimulationController(model, view)

  /**
   * Called when application is closed by X button
   */
  override def stopApp(): Unit = {
    model.stop()
    super.stopApp()
  }
}
