package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.unit.Unit;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public class City extends Living {
  private final HexagonGrid<Tile> grid;
  public Player player;
  public Color wallColour;
  public Color joinColour;

  public ArrayList<Tile> tiles;
  public double greatestTileHeight;

  public ArrayList<Building> buildings;
  public CityBuildable currentlyBuilding;
  public int productionTotal;
  public int citizens;
  public int excessFoodCounter;

  public Unit lastAttacker;

  public String name;

  public City(HexagonGrid<Tile> grid, int centerX, int centerY, Player player) {
    super(200);
    this.grid = grid;
    setOwner(player);
    this.name = "City";

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

    buildings = new ArrayList<>();

    productionTotal = 0;
    citizens = 1;
    excessFoodCounter = 0;
  }

  public City(HexagonGrid<Tile> grid, Map<String, Object> map) {
    super((int) map.get("baseHealth"), (int) map.get("health"));
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
          //noinspection unchecked
          Map<String, Object> improvement = (Map<String, Object>) m.get("improvement");
          center.improvement = Improvement.fromName((String) improvement.get("name"));
          //noinspection unchecked
          center.improvementMetadata = (Map<String, Object>) improvement.get("meta");
        } else {
          center.improvement = Improvement.NONE;
        }
        return center;
      })
      .collect(Collectors.toList());
    tiles.forEach(tile -> tile.city = this);

    //noinspection unchecked
    buildings = (ArrayList<Building>) ((List<String>) map.get("buildings")).stream()
      .map(Building::fromName)
      .collect(Collectors.toList());

    if (map.containsKey("currentlyBuilding")) {
      currentlyBuilding = CityBuildable.fromName((String) map.get("currentlyBuilding"));
    }

    productionTotal = (int) map.get("productionTotal");
    citizens = (int) map.get("citizens");
    excessFoodCounter = (int) map.get("excessFood");
    name = (String) map.get("name");

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
      if (tile.improvement != Improvement.NONE) {
        Map<String, Object> improvementMap = new HashMap<>();
        improvementMap.put("name", tile.improvement.name);
        improvementMap.put("meta", tile.improvementMetadata);
        tileMap.put("improvement", improvementMap);
      }
      tileMaps.add(tileMap);
    }
    map.put("tiles", tileMaps);

    map.put("owner", player.id);

    map.put("buildings", buildings.stream()
      .map(CityBuildable::getName)
      .collect(Collectors.toList()));

    if (currentlyBuilding != null) {
      map.put("currentlyBuilding", currentlyBuilding.getName());
    }

    map.put("productionTotal", productionTotal);
    map.put("citizens", citizens);
    map.put("excessFood", excessFoodCounter);
    map.put("name", name);

    return map;
  }

  public int getProductionPerTurn() {
    int productionPerTurn = 10 + (5 * citizens);
    double multiplier = 1;
    for (Building building : buildings) {
      multiplier *= building.productionPerTurnMultiplier;
    }
    for (Tile tile : tiles) {
      if (tile.improvement != null) {
        productionPerTurn += tile.improvement.productionPerTurn;
      }
    }
    productionPerTurn *= multiplier;
    return productionPerTurn;
  }

  public int getSciencePerTurn() {
    int sciencePerTurn = 5 * citizens;
    double multiplier = 1;
    for (Building building : buildings) {
      sciencePerTurn += building.sciencePerTurnIncrease;
      multiplier *= building.sciencePerTurnMultiplier;
    }
    sciencePerTurn *= multiplier;
    return sciencePerTurn;
  }

  public int getGoldPerTurn() {
    int goldPerTurn = 5 * citizens;
    double multiplier = 1;
    for (Building building : buildings) {
      goldPerTurn += building.goldPerTurnIncrease;
      multiplier *= building.goldPerTurnMultiplier;
    }
    goldPerTurn *= multiplier;
    return goldPerTurn;
  }

  public int getFoodPerTurn() {
    int foodPerTurn = 5 - citizens;
    for (Tile tile : tiles) {
      if (tile.improvement != null) {
        foodPerTurn += tile.improvement.foodPerTurn;
      }
    }
    for (Building building : buildings) {
      foodPerTurn *= building.foodPerTurnMultiplier;
    }
    return foodPerTurn;
  }

  @Override
  public Tile[] handleTurn(Game game) {
    ArrayList<Tile> updatedTiles = new ArrayList<>();

    int productionPerTurn = getProductionPerTurn();
    int sciencePerTurn = getSciencePerTurn();
    int goldPerTurn = getGoldPerTurn();
    int foodPerTurn = getFoodPerTurn();

    productionTotal += productionPerTurn;
    if (currentlyBuilding != null && currentlyBuilding.canBuildWithProduction(productionTotal)) {
      updatedTiles.add(currentlyBuilding.build(this, game));
      productionTotal -= currentlyBuilding.getProductionCost();
      currentlyBuilding = null;
    }

    excessFoodCounter += foodPerTurn;
    double starvationValue = 10 + Math.pow(1.25, citizens - 1);
    double growthValue = 10 + Math.pow(1.25, citizens);
    if (citizens > 1 && excessFoodCounter < starvationValue) {
      citizens--;
    } else if (excessFoodCounter > growthValue) {
      citizens++;
      grow(1);
      updatedTiles.addAll(tiles);
    }

    game.increasePlayerScienceBy(player.id, sciencePerTurn);
    game.increasePlayerGoldBy(player.id, goldPerTurn);

    return updatedTiles.toArray(new Tile[]{});
  }

  @Override
  public void onAttack(Unit attacker, boolean ranged) {
    lastAttacker = attacker;
    damage(attacker.unitType.getAttackStrength());
    if(!ranged) {
      attacker.damage(Math.max(attacker.getHealth() / 4, 10));
    }
  }

  @Override
  public Player getOwner() {
    return player;
  }

  public void setOwner(Player player) {
    this.player = player;
    this.wallColour = player.getColour();
    this.joinColour = this.wallColour.darker();
  }

  @Override
  public Point2D getPosition() {
    return getCenter().getHexagon().getCenter();
  }

  public boolean sameCenterAs(City c) {
    return getCenter().samePositionAs(c.getCenter());
  }
}
