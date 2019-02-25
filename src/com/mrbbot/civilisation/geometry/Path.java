package com.mrbbot.civilisation.geometry;

import java.util.List;

/**
 * Class representing a path between two points
 * @param <E> the type of the data within the path
 */
public class Path<E extends Traversable> {
  /**
   * Ordered list of the tiles in the path
   */
  public final List<E> path;
  /**
   * The cost of travelling to the end through this path
   */
  public final int totalCost;

  Path(List<E> path, int totalCost) {
    this.path = path;
    this.totalCost = totalCost;
  }
}
