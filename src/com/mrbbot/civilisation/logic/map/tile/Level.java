package com.mrbbot.civilisation.logic.map.tile;

/**
 * Enum for level types for tiles. Used to determine which colour to use to
 * render a tile.
 */
public enum Level {
  MOUNTAIN(100, 0.8, 1.0, false),
  PLAIN(1, 0.3, 0.8, false),
  BEACH(1, 0.25, 0.3, false),
  OCEAN(100, 0.0, 0.25, true);

  /**
   * Movement cost of traversing this type of tile.
   */
  public final int cost;
  /**
   * The minimum height for this type of tile
   */
  public final double minHeight;
  /**
   * The maximum height for this type of tile
   */
  public final double maxHeight;
  /**
   * Whether this type of tile should always have the maximum height (only used
   * to maintain constant water height)
   */
  public final boolean fixToMax;

  Level(int cost, double minHeight, double maxHeight, boolean fixToMax) {
    this.cost = cost;
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    this.fixToMax = fixToMax;
  }

  /**
   * Gets the height associated with the specified height
   *
   * @param height height to check
   * @return level associated with the specified height
   */
  static Level of(double height) {
    // Get all possible levels
    Level[] levels = values();
    // Check if the height exceeds the minimum and if it does, return that
    // level
    for (Level level : levels) {
      if (height > level.minHeight) {
        return level;
      }
    }
    return levels[levels.length - 1];
  }
}
