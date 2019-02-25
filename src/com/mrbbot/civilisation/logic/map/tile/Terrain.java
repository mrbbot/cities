package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.NoiseGenerator;
import javafx.geometry.Point2D;

import java.util.Random;

/**
 * Class for the terrain of a tile. Stores information on the height, level, and whether the tile has a tree.
 */
public class Terrain {
  /**
   * Random number generator for trees
   */
  private static final Random RANDOM = new Random();

  /**
   * Height of the tile. This is a number in the range 0 <= height <= 1;
   */
  public double height;
  /**
   * Level associated with the height of the tile. Contains information on the colour of the tile.
   */
  public final Level level;
  /**
   * Whether the tile has a tree in its natural state (regardless of tile improvements that would remove it)
   */
  public boolean hasTree;

  /**
   * Creates a new terrain object for a specified point
   *
   * @param p point to generate terrain for
   */
  Terrain(Point2D p) {
    this(
      // Height of the terrain (rounded to 3 d.p. to reduce file saves)
      Math.round(((NoiseGenerator.getInterpolatedNoise(p.getX(), p.getY()) + 1) / 2) * 1000.0) / 1000.0,
      // Whether the tile has a tree (completely random and not dependent on the position, this is ok as this will only
      // be called once per point during the generation stage)
      RANDOM.nextInt(3) == 0
    );
  }

  /**
   * Cretaes a new terrain object with a specified height and tree
   *
   * @param height  height of the terrain
   * @param hasTree whether the terrain has a tree
   */
  Terrain(double height, boolean hasTree) {
    // Store the height
    this.height = height;

    // Get the level for the height
    level = Level.of(this.height);
    // Set the height to the maximum value if required
    if (level.fixToMax) this.height = level.maxHeight;

    // Only keep the tree if this is on the plains level (we don't want trees in the ocean)
    this.hasTree = hasTree && level == Level.PLAIN;
  }
}
