package ui

import java.text.DecimalFormat

import system.items.ItemType
import system.items.ItemType.ItemType
import ui.Widgets.TitleLabel

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.TextField
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

class ItemPanel extends VBox {

  private class ItemSection(itemType: ItemType, probability: Double) extends HBox {
    private val probabilityText = new TextField {
      text = new DecimalFormat("###.###").format(probability)
    }
    children = Seq(Rectangle(30, 30, itemType.toString), probabilityText)
    alignmentInParent = Pos.Center
    spacing = 20

    def setEnabled(value: Boolean) = probabilityText.setDisable(!value)
    def getProbability: Double = probabilityText.getText.replaceAll(",", ".").toDouble
  }

  private class ProbabilitySection(probability: Double) extends HBox {
    private val items = Map(
      ItemType.Red -> new ItemSection(ItemType.Red, probability),
      ItemType.Green -> new ItemSection(ItemType.Green, probability),
      ItemType.Blue -> new ItemSection(ItemType.Blue, probability))

    children = Seq(items(ItemType.Red), items(ItemType.Green), items(ItemType.Blue))
    alignmentInParent = Pos.Center
    spacing = 20
    padding = Insets(10, 10, 30, 10)

    def setEnabled(value: Boolean) = {
      items.foreach(i => i._2.setEnabled(value))
    }

    def getValues: Map[ItemType, Double] = {
      var m: Map[ItemType, Double] = Map()
      for((itemType, section) <- items) {
        val prob: Double = section.getProbability
        if(prob < 0) throw new IllegalArgumentException("Negative probabilities not allowed!")
        m += (itemType -> prob)
      }
      m
    }
  }

  private val initialProbability: Double = 1.0 / 3.0

  private val producerProbabilities = new ProbabilitySection(initialProbability)
  private val consumerProbabilities = new ProbabilitySection(initialProbability)

  private val errorLabel = new TitleLabel("") {
    minWidth = 100
    textFill = Color.Red
  }

  children = Seq(
    new TitleLabel("Producer item probabilities:"),
    producerProbabilities,
    new TitleLabel("Consumer item probabilities:"),
    consumerProbabilities,
    errorLabel)
  style = Widgets.borderStyle

  def setEnabled(value: Boolean): Unit = {
    producerProbabilities.setEnabled(value)
    consumerProbabilities.setEnabled(value)
  }

  def getProducerProbabilities: Option[Map[ItemType, Double]] = {
    getProbabilities(producerProbabilities)
  }

  def getConsumerProbabilities: Option[Map[ItemType, Double]] = {
    getProbabilities(consumerProbabilities)
  }

  private def getProbabilities(ps: ProbabilitySection): Option[Map[ItemType, Double]] = {
    try {
      val rv: Map[ItemType, Double] = ps.getValues
      errorLabel.setText("")
      return Some(rv)
    }
    catch {
      case e: Exception =>
        println(e.toString)
        errorLabel.setText("Invalid input!")
    }
    None
  }
}