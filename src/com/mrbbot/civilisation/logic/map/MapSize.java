package com.mrbbot.civilisation.logic.map;

public enum MapSize {
  TINY("Tiny", 5, 4),
  SMALL("Small", 10, 7),
  STANDARD("Standard", 20, 17),
  LARGE("Large", 30, 25);

  public String name;
  public int width;
  public int height;

  MapSize(String name, int width, int height) {
    this.name = name;
    this.width = width;
    this.height = height;
  }
}
