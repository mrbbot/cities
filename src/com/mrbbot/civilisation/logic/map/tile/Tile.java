package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.Hexagon;
import com.mrbbot.civilisation.logic.interfaces.Traversable;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.render.map.RenderTile;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Tile implements Traversable {
  private final Hexagon hexagon;
  public final int x;
  public final int y;
  private final Terrain terrain;
  public City city;
  public Improvement improvement;
  public Map<String, Object> improvementMetadata = new HashMap<>();
  public Unit unit;
  public boolean selected = false;

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
    return terrain.level.cost;
  }

  @Override
  public boolean canTraverse() {
    return terrain.level != Level.OCEAN && terrain.level != Level.MOUNTAIN && unit == null;
  }

  public boolean samePositionAs(Tile t) {
    return x == t.x && y == t.y;
  }

  @Override
  public String toString() {
    return String.format("Tile[x=%d, y=%d]", x, y);
  }
}
