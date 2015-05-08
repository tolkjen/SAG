package system.level

/** Thrown when there is no way to reach a given destination point(s) on the level map. */
class PathNotFoundException extends Exception("Path was not found")
