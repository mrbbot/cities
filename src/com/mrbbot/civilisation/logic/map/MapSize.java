package com.mrbbot.civilisation.logic.map;

/**
 * Enum for the different sizes a a map can be, used by the host/join screen to
 * list available sizes.
 */
public enum MapSize {
  TINY("Tiny", 5, 4),
  SMALL("Small", 10, 7),
  STANDARD("Standard", 20, 17),
  LARGE("Large", 30, 25);

  /**
   * User facing name of the map size
   */
  public String name;
  /**
   * Width of the hexagon grid for this size
   */
  public int width;
  /**
   * Height of the hexagon grid for this size
   */
  public int height;

  MapSize(String name, int width, int height) {
    this.name = name;
    this.width = width;
    this.height = height;
  }
}
