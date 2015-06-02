package system.items

/**
 * @param averageTimeMillis time in milliseconds
 * @param totalItems number of items taken from producer or delivered to consumer
 */
class Statistic(val averageTimeMillis: Long, val totalItems: Int) {}