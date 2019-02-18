package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.NoiseGenerator;
import javafx.geometry.Point2D;

public class Terrain {
  public double height; // 0 <= height <= 1
  public final Level level;

  Terrain(Point2D p) {
    this(Math.round(((NoiseGenerator.getInterpolatedNoise(p.getX(), p.getY()) + 1) / 2) * 1000.0) / 1000.0);
  }

  Terrain(double height) {
    this.height = height;

    level = Level.of(this.height);
    if (level.fixToMax) this.height = level.maxHeight;
  }
}
