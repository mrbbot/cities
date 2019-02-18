package com.mrbbot.civilisation.logic.map.tile;

import java.util.Random;

public enum Level {
  MOUNTAIN(100, 0.8, 1.0, false),
  PLAIN(2, 0.3, 0.8, false),
  BEACH(2, 0.25, 0.3, false),
  OCEAN(100, 0.0, 0.25, true);

  public final int cost;
  public final double minHeight;
  public final double maxHeight;
  public final boolean fixToMax;

  Level(int cost, double minHeight, double maxHeight, boolean fixToMax) {
    this.cost = cost;
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    this.fixToMax = fixToMax;
  }

  static Level of(double height) {
    Level[] levels = values();
    for (Level level : levels) {
      if (height > level.minHeight) {
        return level;
      }
    }
    return levels[levels.length - 1];
  }

  private static final Random RANDOM = new Random();

  static Level random() {
    Level[] levels = values();
    return levels[RANDOM.nextInt(levels.length)];
  }
}
