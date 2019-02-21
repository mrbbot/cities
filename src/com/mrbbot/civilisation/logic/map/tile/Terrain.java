package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.NoiseGenerator;
import javafx.geometry.Point2D;

import java.util.Random;

public class Terrain {
  private static final Random RANDOM = new Random();

  public double height; // 0 <= height <= 1
  public final Level level;
  public boolean hasTree;

  Terrain(Point2D p) {
    this(Math.round(((NoiseGenerator.getInterpolatedNoise(p.getX(), p.getY()) + 1) / 2) * 1000.0) / 1000.0, RANDOM.nextInt(3) == 0);
  }

  Terrain(double height, boolean hasTree) {
    this.height = height;
    this.hasTree = hasTree;

    level = Level.of(this.height);
    if (level.fixToMax) this.height = level.maxHeight;
  }
}
