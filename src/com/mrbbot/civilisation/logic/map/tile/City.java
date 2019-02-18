package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public class City extends Living {
  private final HexagonGrid<Tile> grid;
  public Player player;
  public final Color wallColour;
  public final Color joinColour;

  public ArrayList<Tile> tiles;
  public double greatestTileHeight;

  public City(HexagonGrid<Tile> grid, int centerX, int centerY, Player player) {
    super(100);
    this.grid = grid;
    this.player = player;
    this.wallColour = player.getColour();
    this.joinColour = this.wallColour.darker();

    tiles = new ArrayList<>();

    Tile center = grid.get(centerX, centerY);
    if (center.city != null) {
      throw new IllegalArgumentException("City created on tile with another city");
    }
    tiles.add(center);
    center.improvement = Improvement.CAPITAL;
//    if(center.renderer != null) center.renderer.updateImprovement();

    ArrayList<Tile> adjacentTiles = grid.getNeighbours(centerX, centerY, false);
    adjacentTiles.removeIf(tile -> tile.city != null);
    tiles.addAll(adjacentTiles);

    tiles.forEach(tile -> tile.city = this);
    updateGreatestHeight();
  }

  public City(HexagonGrid<Tile> grid, Map<String, Object> map) {
    super((int) map.get("baseHealth"));
    this.health = (int) map.get("health");
    this.grid = grid;
    this.player = new Player((String) map.get("owner"));
    this.wallColour = player.getColour();
    this.joinColour = this.wallColour.darker();

    //noinspection unchecked
    tiles = (ArrayList<Tile>) ((List<Map<String, Object>>) map.get("tiles")).stream()
      .map(m -> {
        int x = (int) m.get("x");
        int y = (int) m.get("y");
        Tile center = grid.get(x, y);
        if(m.containsKey("improvement")) {
          center.improvement = Improvement.valueOf((String) m.get("improvement"));
        }
        return center;
      })
      .collect(Collectors.toList());

    tiles.forEach(tile -> tile.city = this);
    updateGreatestHeight();
  }

  public ArrayList<Point2D> grow(int newTiles) {
    final ArrayList<Point2D> grownTo = new ArrayList<>();

    final Point2D center = getCenter().getHexagon().getCenter();

    PriorityQueue<Tile> potentialTiles = new PriorityQueue<>((a, b) -> {
      double aDist = center.distance(a.getHexagon().getCenter());
      double bDist = center.distance(b.getHexagon().getCenter());
      return Double.compare(aDist, bDist);
    });

    tiles.forEach(tile -> potentialTiles.addAll(grid.getNeighbours(tile.x, tile.y, false)
      .parallelStream()
      .filter(adjTile -> adjTile.city == null)
      .collect(Collectors.toList())));

    while (newTiles > 0 && potentialTiles.size() >= newTiles) {
      Tile tile = potentialTiles.remove();
      tile.city = this;
      tiles.add(tile);
      grownTo.add(new Point2D(tile.x, tile.y));
      newTiles--;
    }

    updateGreatestHeight();

    return grownTo;
  }

  public void growTo(ArrayList<Point2D> points) {
    for (Point2D point : points) {
      Tile tile = grid.get((int) point.getX(), (int) point.getY());
      tile.city = this;
      tiles.add(tile);
    }
    updateGreatestHeight();
  }

  boolean[] getWalls(Tile tile) {
    if (!tiles.contains(tile)) return new boolean[]{false, false, false, false, false, false};
    int x = tile.x, y = tile.y;
    return new boolean[]{
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
      if (tileHeight > greatestHeight) greatestHeight = tileHeight;
    }
    greatestTileHeight = greatestHeight;
  }

  public Tile getCenter() {
    return tiles.get(0);
  }

  @Override
  public int getX() {
    return getCenter().x;
  }

  @Override
  public int getY() {
    return getCenter().y;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> map = super.toMap();

    ArrayList<Map<String, Object>> tileMaps = new ArrayList<>();
    for (Tile tile : tiles) {
      Map<String, Object> tileMap = new HashMap<>();
      tileMap.put("x", tile.x);
      tileMap.put("y", tile.y);
      if(tile.improvement != Improvement.NONE) {
        tileMap.put("improvement", tile.improvement.toString());
      }
      tileMaps.add(tileMap);
    }
    map.put("tiles", tileMaps);

    map.put("owner", player.id);

    return map;
  }
}
