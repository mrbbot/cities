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

  public Tile(Hexagon hexagon, int x, int y) {
    this.hexagon = hexagon;
    this.x = x;
    this.y = y;
    this.terrain = new Terrain(hexagon.getCenter());
    this.improvement = this.terrain.hasTree ? Improvement.TREE : Improvement.NONE;
  }

  public Tile(Hexagon hexagon, int x, int y, double height, boolean hasTree) {
    this.hexagon = hexagon;
    this.x = x;
    this.y = y;
    this.terrain = new Terrain(height, hasTree);
    this.improvement = this.terrain.hasTree ? Improvement.TREE : Improvement.NONE;
  }

  public Hexagon getHexagon() {
    return hexagon;
  }

  public boolean[] getCityWalls() {
    return city.getWalls(this);
  }

  public double getHeight() {
    return (terrain.height * 2) + 1; // 1 <= height <= 3
  }

  public void setImprovement(Improvement improvement) {
    this.improvement = improvement;
    renderer.updateImprovement();
  }

  @Override
  public int getX() {
    return x;
  }

  @Override
  public int getY() {
    return y;
  }

  public Terrain getTerrain() {
    return terrain;
  }

  @Override
  public int getCost() {
    return hasRoad() ? 0 : terrain.level.cost;
  }

  @Override
  public boolean canTraverse() {
    return terrain.level != Level.OCEAN && terrain.level != Level.MOUNTAIN && unit == null;
  }

  public boolean samePositionAs(Tile t) {
    return x == t.x && y == t.y;
  }

  public boolean hasRoad() {
    return improvement == Improvement.CAPITAL || improvement == Improvement.ROAD;
  }

  @Override
  public String toString() {
    return String.format("Tile[x=%d, y=%d]", x, y);
  }
}
