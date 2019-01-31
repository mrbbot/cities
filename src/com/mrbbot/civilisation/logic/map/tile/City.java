package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.net.serializable.SerializablePoint2D;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.Collectors;

public class City implements Serializable {
  private final HexagonGrid<Tile> grid;
  public final Color wallColour;
  public final Color joinColour;

  public ArrayList<Tile> tiles;
  public double greatestTileHeight;

  public City(HexagonGrid<Tile> grid, int centerX, int centerY, Color wallColour) {
    this.grid = grid;
    this.wallColour = wallColour;
    this.joinColour = wallColour.darker();

    tiles = new ArrayList<>();

    Tile center = grid.get(centerX, centerY);
    if(center.city != null) {
      throw new IllegalArgumentException("City created on tile with another city");
    }
    tiles.add(center);
    center.improvement = Improvement.CAPITAL;

    ArrayList<Tile> adjacentTiles = grid.getNeighbours(centerX, centerY, false);
    adjacentTiles.removeIf(tile -> tile.city != null);
    tiles.addAll(adjacentTiles);

    tiles.forEach(tile -> tile.city = this);

    updateGreatestHeight();
  }

  public void grow(int newTiles) {
    final SerializablePoint2D center = getCenter().getHexagon().getCenter();

    PriorityQueue<Tile> potentialTiles = new PriorityQueue<>((a, b) -> {
      double aDist = center.point.distance(a.getHexagon().getCenter().point);
      double bDist = center.point.distance(b.getHexagon().getCenter().point);
      return Double.compare(aDist, bDist);
    });

    tiles.forEach(tile -> potentialTiles.addAll(grid.getNeighbours(tile.x, tile.y, false)
      .parallelStream()
      .filter(adjTile -> adjTile.city == null)
      .collect(Collectors.toList())));

    while(newTiles > 0 && potentialTiles.size() >= newTiles) {
      Tile tile = potentialTiles.remove();
      tile.city = this;
      tiles.add(tile);
      newTiles--;
    }

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
