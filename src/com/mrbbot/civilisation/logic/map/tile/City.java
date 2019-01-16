package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.HexagonGrid;

import java.util.ArrayList;

public class City {
  private final HexagonGrid<Tile> grid;

  public ArrayList<Tile> tiles;
  public double greatestTileHeight;

  public City(HexagonGrid<Tile> grid, int centerX, int centerY) {
    this.grid = grid;

    tiles = new ArrayList<>();

    Tile center = grid.get(centerX, centerY);
    if(center.city != null) {
      throw new IllegalArgumentException("City created on tile with another city");
    }
    tiles.add(center);

    ArrayList<Tile> adjacentTiles = grid.getNeighbours(centerX, centerY, false);
    adjacentTiles.removeIf(tile -> tile.city != null);
    tiles.addAll(adjacentTiles);

    tiles.forEach(tile -> tile.city = this);
    updateGreatestHeight();
  }

  boolean[] getWalls(Tile tile) {
    if(!tiles.contains(tile)) return new boolean[]{false, false, false, false, false, false};
    int x = tile.x, y = tile.y;
    return new boolean[] {
      !tiles.contains(grid.getTopLeft(x, y, false)),
      !tiles.contains(grid.getLeft(x, y, false)),
      !tiles.contains(grid.getBottomLeft(x, y, false)),
      !tiles.contains(grid.getBottomRight(x, y, false)),
      !tiles.contains(grid.getRight(x, y, false)),
      !tiles.contains(grid.getTopRight(x, y, false)),
    };
  }

  private void updateGreatestHeight() {
    double greatestHeight = 0.0;
    for (Tile tile : tiles) {
      double tileHeight = tile.getHeight();
      if(tileHeight > greatestHeight) greatestHeight = tileHeight;
    }
    greatestTileHeight = greatestHeight;
  }

  public Tile getCenter() {
    return tiles.get(0);
  }
}
