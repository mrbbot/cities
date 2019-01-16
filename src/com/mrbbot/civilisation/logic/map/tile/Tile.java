package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.Hexagon;
import com.mrbbot.civilisation.logic.map.Traversable;
import com.mrbbot.civilisation.render.map.RenderTile;

public class Tile implements Traversable {
  private final Hexagon hexagon;
  public final int x;
  public final int y;
  private final Terrain terrain;
  public City city;

  public RenderTile renderer;

  public Tile(Hexagon hexagon, int x, int y) {
    this.hexagon = hexagon;
    this.x = x;
    this.y = y;

    this.terrain = new Terrain(hexagon.getCenter());
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
    return terrain.level != Level.OCEAN && terrain.level != Level.MOUNTAIN;
  }
}
