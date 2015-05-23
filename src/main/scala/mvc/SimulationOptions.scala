package mvc

import system.items.ItemType._
import system.robot.RobotType._

/**
 * Options with which the simulation should be started.
 *
 * @param producerProbabilities for each item type the probability that the producer will create it
 * @param consumerProbabilities for each item type the probability that the consumer will demand it
 * Probabilities can be any non-negative numbers - they should be normalized to 0..1 range in producer/consumer
 * @param robotCounts number of robots of each type
 * @param communicationRadius range of robots' wireless communication
 */
class SimulationOptions(val producerProbabilities: Map[ItemType, Double],
                        val consumerProbabilities: Map[ItemType, Double],
                        val robotCounts: Map[RobotType, Int],
                        val communicationRadius: Int) {}