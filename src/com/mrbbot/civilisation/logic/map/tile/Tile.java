package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.Hexagon;
import com.mrbbot.civilisation.geometry.Traversable;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.render.map.RenderTile;
import com.mrbbot.generic.net.ClientOnly;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for a tile of the map. Stored in the game's hexagon grid.
 */
public class Tile implements Traversable {
  /**
   * Hexagon this tile represents
   */
  private final Hexagon hexagon;
  /**
   * X-coordinate of this tile within the hexagon grid
   */
  public final int x;
  /**
   * Y-coordinate of this tile within the hexagon grid
   */
  public final int y;
  /**
   * Terrain object for this tile
   */
  private final Terrain terrain;
  /**
   * The city this tile is part of. May be null.
   */
  public City city;
  /**
   * This tiles improvement. Defaults to {@link Improvement#NONE}.
   */
  public Improvement improvement;
  /**
   * Metadata associated with the improvement (angle, strip count, width).
   */
  public Map<String, Object> improvementMetadata = new HashMap<>();
  /**
   * Unit currently on the tile. May be null.
   */
  public Unit unit;
  /**
   * Whether this tile is the currently selected tile.
   */
  @ClientOnly
  public boolean selected = false;
  /**
   * Renderer for this tile. Only used by the client.
   */
  @ClientOnly
  public RenderTile renderer;

  /**
   * Creates a new tile object for the specified coordinate
   *
   * @param hexagon hexagon the tile is part of
   * @param x       x-coordinate of the tile
   * @param y       y-coordinate of the tile
   */
  public Tile(Hexagon hexagon, int x, int y) {
    this.hexagon = hexagon;
    this.x = x;
    this.y = y;
    this.terrain = new Terrain(hexagon.getCenter());
    // Set the improvement to a tree if there is one
    this.improvement = this.terrain.hasTree
      ? Improvement.TREE
      : Improvement.NONE;
  }

  /**
   * Creates a new tile object for the specified coordinates with a pre-set
   * height and tree state
   *
   * @param hexagon hexagon the tile is part of
   * @param x       x-coordinate of the tile
   * @param y       y-coordinate of the tile
   * @param height  height of the tile's terrain
   * @param hasTree whether the tile naturally has a tree
   */
  public Tile(Hexagon hexagon, int x, int y, double height, boolean hasTree) {
    this.hexagon = hexagon;
    this.x = x;
    this.y = y;
    this.terrain = new Terrain(height, hasTree);
    // Set the improvement to a tree if there is one
    this.improvement = this.terrain.hasTree
      ? Improvement.TREE
      : Improvement.NONE;
  }

  /**
   * Gets the tiles hexagon
   *
   * @return hexagon attached to the tile
   */
  public Hexagon getHexagon() {
    return hexagon;
  }

  /**
   * Gets the city walls for this tile
   *
   * @return boolean array containing details on whether adjacent edges belong
   * to the same city
   */
  public boolean[] getCityWalls() {
    assert city != null;
    return city.getWalls(this);
  }

  /**
   * Gets the actual height of this tile
   *
   * @return terrain height mapped onto range [1, 3]
   */
  public double getHeight() {
    return (terrain.height * 2) + 1; // 1 <= height <= 3
  }

  /**
   * Gets the x-coordinate of this tile
   *
   * @return x-coordinate of this tile
   */
  @Override
  public int getX() {
    return x;
  }

  /**
   * Gets the y-coordinate of this tile
   *
   * @return y-coordinate of this tile
   */
  @Override
  public int getY() {
    return y;
  }

  /**
   * Gets the terrain object associated with this tile
   *
   * @return terrain object associated with this tile
   */
  public Terrain getTerrain() {
    return terrain;
  }

  /**
   * Gets the cost of travelling over this tile. Returns the tile cost or 0 if
   * the tile has a road.
   *
   * @return cost of travelling over this tile
   */
  @Override
  public int getCost() {
    return hasRoad() ? 0 : terrain.level.cost;
  }

  /**
   * Checks whether a unit could actually traverse this tile
   *
   * @return traversability of this tile
   */
  @Override
  public boolean canTraverse() {
    // Traversable if the level isn't an ocean or mountain, and if there isn't
    // already a unit on the tile
    return terrain.level != Level.OCEAN
      && terrain.level != Level.MOUNTAIN
      && unit == null;
  }

  /**
   * Checks whether another tile has the same position as this tile
   *
   * @param t other tile to check
   * @return whether the 2 tiles have the same position
   */
  public boolean samePositionAs(Tile t) {
    return x == t.x && y == t.y;
  }

  /**
   * Checks whether this tile has a road. Capitals have a road by default.
   *
   * @return whether this tile has a road
   */
  public boolean hasRoad() {
    // Check if the improvement is a capital or an actual road
    return improvement == Improvement.CAPITAL
      || improvement == Improvement.ROAD;
  }

  @Override
  public String toString() {
    return String.format("Tile[x=%d, y=%d]", x, y);
  }
}
