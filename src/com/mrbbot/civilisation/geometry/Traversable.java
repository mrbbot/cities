package com.mrbbot.civilisation.geometry;

/**
 * Interface representing a traversable element
 */
public interface Traversable extends Positionable {
  /**
   * Gets the cost of traversing this element
   *
   * @return cost of traversing this element
   */
  int getCost();

  /**
   * Checks whether this tile can be traversed
   *
   * @return whether this tile can be traversed
   */
  boolean canTraverse();
}
