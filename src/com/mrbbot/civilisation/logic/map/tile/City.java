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
  /**
   * Hexagon grid the city is contained within
   */
  private final HexagonGrid<Tile> grid;
  /**
   * Player the city is owned by
   */
  public Player player;
  /**
   * Colour of the walls of the city
   */
  public Color wallColour;
  /**
   * Colour of the wall joins of the city
   */
  public Color joinColour;

  /**
   * Tiles that the city owns
   */
  public ArrayList<Tile> tiles;
  /**
   * Height of the tile with the greatest height
   */
  public double greatestTileHeight;

  /**
   * Buildings the city has
   */
  public ArrayList<Building> buildings;
  /**
   * What the city is currently building
   */
  public CityBuildable currentlyBuilding;
  /**
   * The current production total in the city. When this value reaches the
   * currentlyBuilding's cost it will be built.
   */
  public int productionTotal;
  /**
   * The number of citizens within the city. Used to calculated gold/science
   * per turn.
   */
  public int citizens;
  /**
   * The amount of excess food the city has. Controls when the city
   * grows/starves.
   */
  public int excessFoodCounter;

  /**
   * The last unit that attacked this city. Used to control who to give the
   * city to if its health reaches 0.
   */
  public Unit lastAttacker;

  /**
   * The name of the city. Can be edited by the player.
   */
  public String name;

  /**
   * Constructor for a new city
   *
   * @param grid    hexagon grid for the game
   * @param centerX the center x-coordinate of the city
   * @param centerY the center y-coordinate of the city
   * @param player  the player who owns this city
   */
  public City(HexagonGrid<Tile> grid, int centerX, int centerY, Player player) {
    // Pass required parameters to base living class
    super(200);
    // Store passed values
    this.grid = grid;
    setOwner(player);
    this.name = "City";

    // Create empty list for tiles
    tiles = new ArrayList<>();

    // Get center and check it doesn't already have a city
    Tile center = grid.get(centerX, centerY);
    if (center.city != null) {
      throw new IllegalArgumentException(
        "City created on tile with another city"
      );
    }
    tiles.add(center);
    // Make the center of the city a capital
    center.improvement = Improvement.CAPITAL;

    // Add all of the adjacent tiles that don't already have a city
    ArrayList<Tile> adjacentTiles = grid.getNeighbours(
      centerX, centerY,
      false
    );
    adjacentTiles.removeIf(tile -> tile.city != null);
    tiles.addAll(adjacentTiles);

    // Mark all of the cities tiles as belonging to this city
    tiles.forEach(tile -> tile.city = this);
    // Calculate the greatest height of all the tiles
    updateGreatestHeight();

    // Create empty list for buildings
    buildings = new ArrayList<>();

    // Reset totals
    productionTotal = 0;
    citizens = 1;
    excessFoodCounter = 0;
  }

  /**
   * Constructor for a city loaded from a Map (could be from a file/server)
   *
   * @param grid hexagon grid for the game
   * @param map  map containing city data
   */
  public City(HexagonGrid<Tile> grid, Map<String, Object> map) {
    // Pass required parameters to base living class
    super((int) map.get("baseHealth"), (int) map.get("health"));
    // Store passed values
    this.grid = grid;

    // Load the city owner
    setOwner(new Player((String) map.get("owner")));

    // Load the tiles belonging to the city
    //noinspection unchecked
    tiles = (ArrayList<Tile>) ((List<Map<String, Object>>) map.get("tiles"))
      .stream()
      .map(m -> {
        // Get the tile with the specified coordinates
        int x = (int) m.get("x");
        int y = (int) m.get("y");
        Tile center = grid.get(x, y);

        // Load the tile's improvement if there is one
        if (m.containsKey("improvement")) {
          //noinspection unchecked
          Map<String, Object> improvement =
            (Map<String, Object>) m.get("improvement");
          center.improvement =
            Improvement.fromName((String) improvement.get("name"));
          //noinspection unchecked
          center.improvementMetadata =
            (Map<String, Object>) improvement.get("meta");
        } else {
          // Otherwise set the improvement to none
          center.improvement = Improvement.NONE;
        }
        return center;
      })
      .collect(Collectors.toList());
    // Mark the tiles as belonging to this city
    tiles.forEach(tile -> tile.city = this);

    // Load the buildings the city has
    //noinspection unchecked
    buildings = (ArrayList<Building>) ((List<String>) map.get("buildings"))
      .stream()
      .map(Building::fromName)
      .collect(Collectors.toList());

    // Load the current build of the city if there is one
    if (map.containsKey("currentlyBuilding")) {
      currentlyBuilding =
        CityBuildable.fromName((String) map.get("currentlyBuilding"));
    }

    // Load totals from the map
    productionTotal = (int) map.get("productionTotal");
    citizens = (int) map.get("citizens");
    excessFoodCounter = (int) map.get("excessFood");

    // Load the city name
    name = (String) map.get("name");

    // Calculate the greatest height of all the tiles belonging to the city
    updateGreatestHeight();
  }

  /**
   * Grow the city by the specified number of nearby tiles
   *
   * @param newTiles number of tiles to grow
   * @return the points of the tiles the city grew too
   */
  public ArrayList<Point2D> grow(int newTiles) {
    final ArrayList<Point2D> grownTo = new ArrayList<>();

    // Get the center coordinate of the city
    final Point2D center = getCenter().getHexagon().getCenter();

    // Create a new queue to pull potential tiles from that sorts tiles by
    // their distance from the center
    PriorityQueue<Tile> potentialTiles = new PriorityQueue<>((a, b) -> {
      double aDist = center.distance(a.getHexagon().getCenter());
      double bDist = center.distance(b.getHexagon().getCenter());
      return Double.compare(aDist, bDist);
    });

    // Add all adjacent tiles that don't have a city to the potential tile list
    tiles.forEach(tile -> potentialTiles.addAll(
      grid.getNeighbours(tile.x, tile.y, false)
        .stream()
        .filter(adjTile -> adjTile.city == null)
        .collect(Collectors.toList()))
    );

    // Keep picking tiles from the queue until the specified number of tiles
    // have been picked
    while (newTiles > 0 && potentialTiles.size() >= newTiles) {
      // Get the next tile
      Tile tile = potentialTiles.remove();
      // Mark it as belonging to this city and add it
      tile.city = this;
      tiles.add(tile);
      // Add the grown coordinate to the list of tiles
      grownTo.add(new Point2D(tile.x, tile.y));
      newTiles--;
    }

    // Calculate the new greatest height
    updateGreatestHeight();

    // Return the list of grown to coordinates
    return grownTo;
  }

  /**
   * Grow the city to the tiles pointed to by the points list
   *
   * @param points list of coordinates of tiles to grow to
   */
  public void growTo(ArrayList<Point2D> points) {
    for (Point2D point : points) {
      // Get the tile represented by the point
      Tile tile = grid.get((int) point.getX(), (int) point.getY());
      // Mark it as belonging to this city and add it
      tile.city = this;
      tiles.add(tile);
    }
    // Calculate the new greatest height
    updateGreatestHeight();
  }

  /**
   * Get the directions from the center that should have walls (that is,
   * adjacent directions that don't belong to the
   * city)
   *
   * @param tile tile to get walls from
   * @return boolean array of whether the tile should have walls in that
   * direction
   */
  boolean[] getWalls(Tile tile) {
    // If this tile isn't part of the city, return an "empty" array
    if (!tiles.contains(tile))
      return new boolean[]{false, false, false, false, false, false};
    // Alias the x and y coordinates
    int x = tile.x, y = tile.y;
    // Return the array for all of the directions, checking if the tile in each
    // direction belongs to this city.
    return new boolean[]{
      !tiles.contains(grid.getTopLeft(x, y, false)),
      !tiles.contains(grid.getLeft(x, y, false)),
      !tiles.contains(grid.getBottomLeft(x, y, false)),
      !tiles.contains(grid.getBottomRight(x, y, false)),
      !tiles.contains(grid.getRight(x, y, false)),
      !tiles.contains(grid.getTopRight(x, y, false)),
    };
  }

  /**
   * Calculates the greatest height of a tile in the city, used for rendering
   * walls
   */
  private void updateGreatestHeight() {
    greatestTileHeight = tiles.stream()
      .map(Tile::getHeight)
      .max(Double::compareTo)
      .orElse(0.0);
  }

  /**
   * Get the center/capital of this city
   *
   * @return Tile representing the center
   */
  public Tile getCenter() {
    // The tile is always the first element added to the tile list
    return tiles.get(0);
  }

  /**
   * Gets the x-coordinate of the center
   *
   * @return x-coordinate of the center
   */
  @Override
  public int getX() {
    return getCenter().x;
  }

  /**
   * Gets the y-coordinate of the center
   *
   * @return y-coordinate of the center
   */
  @Override
  public int getY() {
    return getCenter().y;
  }

  /**
   * Builds a Map containing all required information to rebuild the city
   *
   * @return Map containing city information
   */
  @Override
  public Map<String, Object> toMap() {
    // Get details on the health of the city (from Living)
    Map<String, Object> map = super.toMap();

    // Store tile information in the map
    ArrayList<Map<String, Object>> tileMaps = new ArrayList<>();
    for (Tile tile : tiles) {
      Map<String, Object> tileMap = new HashMap<>();
      // Store the coordinate
      tileMap.put("x", tile.x);
      tileMap.put("y", tile.y);
      // Store the improvement if there is one
      if (tile.improvement != Improvement.NONE) {
        Map<String, Object> improvementMap = new HashMap<>();
        improvementMap.put("name", tile.improvement.name);
        improvementMap.put("meta", tile.improvementMetadata);
        tileMap.put("improvement", improvementMap);
      }
      tileMaps.add(tileMap);
    }
    map.put("tiles", tileMaps);

    // Store the city owner
    map.put("owner", player.id);

    // Store the city's buildings
    map.put("buildings", buildings.stream()
      .map(CityBuildable::getName)
      .collect(Collectors.toList()));

    // Store the current building project if there is one
    if (currentlyBuilding != null) {
      map.put("currentlyBuilding", currentlyBuilding.getName());
    }

    // Store the totals
    map.put("productionTotal", productionTotal);
    map.put("citizens", citizens);
    map.put("excessFood", excessFoodCounter);

    // Store the city name
    map.put("name", name);

    return map;
  }

  /**
   * Gets the production per turn produced by this city. Calculated from the
   * number of citizens, buildings with production bonuses, and tile
   * improvements owned by the city.
   *
   * @return city's production per turn
   */
  public int getProductionPerTurn() {
    // Calculate the base production per turn from the citizen count
    int productionPerTurn = 10 + (5 * citizens);
    // Add all tile improvements to the production counter
    for (Tile tile : tiles) {
      if (tile.improvement != null) {
        productionPerTurn += tile.improvement.productionPerTurn;
      }
    }
    // Multiply the counter by all buildings with production multipliers
    for (Building building : buildings) {
      productionPerTurn *= building.productionPerTurnMultiplier;
    }
    return productionPerTurn;
  }

  /**
   * Gets the science per turn produced by this city. Calculated from the
   * number of citizens and buildings with science bonuses.
   *
   * @return city's science per turn
   */
  public int getSciencePerTurn() {
    // Calculate the base science per turn from the citizen count
    int sciencePerTurn = 5 * citizens;
    // Initialise the multiplier
    double multiplier = 1;
    for (Building building : buildings) {
      // Add to the total science per turn
      sciencePerTurn += building.sciencePerTurnIncrease;
      multiplier *= building.sciencePerTurnMultiplier;
    }
    // Apply the multiplier after all increases have been added
    sciencePerTurn *= multiplier;
    return sciencePerTurn;
  }

  /**
   * Gets the gold per turn provided by this city. Calculated from the number
   * of citizens and buildings with gold bonuses.
   *
   * @return city's gold per turn
   */
  public int getGoldPerTurn() {
    // Calculate the base gold per turn from the citizen count
    int goldPerTurn = 5 * citizens;
    // Initialise the multiplier
    double multiplier = 1;
    for (Building building : buildings) {
      // Add to the total gold per turn
      goldPerTurn += building.goldPerTurnIncrease;
      multiplier *= building.goldPerTurnMultiplier;
    }
    // Apply the multiplier after all increases have been added
    goldPerTurn *= multiplier;
    return goldPerTurn;
  }

  /**
   * Gets the food per turn for this city. Calculated from the number of
   * citizens and buildings and tiles with food bonuses.
   *
   * @return city's food per turn
   */
  public int getFoodPerTurn() {
    // Start with 5 base food per turn and subtract the number of citizens from
    // this
    int foodPerTurn = 5 - citizens;
    // Apply tile bonuses
    for (Tile tile : tiles) {
      if (tile.improvement != null) {
        foodPerTurn += tile.improvement.foodPerTurn;
      }
    }
    // Apply building multipliers
    for (Building building : buildings) {
      foodPerTurn *= building.foodPerTurnMultiplier;
    }
    return foodPerTurn;
  }

  /**
   * Handle a city's per turn operations
   *
   * @param game game object containing this city
   * @return tiles that have been updated this turn
   */
  @Override
  public Tile[] handleTurn(Game game) {
    ArrayList<Tile> updatedTiles = new ArrayList<>();

    // Get totals for resources
    int productionPerTurn = getProductionPerTurn();
    int sciencePerTurn = getSciencePerTurn();
    int goldPerTurn = getGoldPerTurn();
    int foodPerTurn = getFoodPerTurn();

    // Add the production total and check if the currently building thing can
    // now be built
    productionTotal += productionPerTurn;
    if (
      currentlyBuilding != null &&
        currentlyBuilding.canBuildWithProduction(productionTotal)
    ) {
      // If it can build it
      updatedTiles.add(currentlyBuilding.build(this, game));
      // Remove the cost from the total
      productionTotal -= currentlyBuilding.getProductionCost();
      // Reset the currently building item
      currentlyBuilding = null;
    }

    // Add/subtract food to the counter
    excessFoodCounter += foodPerTurn;
    // Calculate growth/starvation values
    double starvationValue = 10 + Math.pow(1.25, citizens - 1);
    double growthValue = 10 + Math.pow(1.25, citizens);
    // Check if the city should grow/starve
    if (citizens > 1 && excessFoodCounter < starvationValue) {
      citizens--;
    } else if (excessFoodCounter > growthValue) {
      citizens++;
      // If growing, grow the city by an extra tile and mark the grown tiles
      // for updating
      grow(1);
      updatedTiles.addAll(tiles);
    }

    // Increase global player science/gold counts by the counts for this city
    game.increasePlayerScienceBy(player.id, sciencePerTurn);
    game.increasePlayerGoldBy(player.id, goldPerTurn);

    // Return all the tiles updated this turn
    return updatedTiles.toArray(new Tile[]{});
  }

  /**
   * Handle when a unit attacks this city
   *
   * @param attacker unit that is attacking
   * @param ranged   whether the unit performed a ranged attack
   */
  @Override
  public void onAttack(Unit attacker, boolean ranged) {
    // Mark the attack as the last attacker to this city
    lastAttacker = attacker;
    // Damage the city the correct amount
    damage(attacker.unitType.getAttackStrength());
    // If this wasn't a ranged attack, damage the attacker too
    if (!ranged) {
      attacker.damage(Math.max(attacker.getHealth() / 4, 10));
    }
  }

  /**
   * Gets the owner of this city
   *
   * @return owner of this city
   */
  @Override
  public Player getOwner() {
    return player;
  }

  /**
   * Sets the owner of this city, updating the wall and join colours
   *
   * @param player new owner of this city
   */
  public void setOwner(Player player) {
    this.player = player;
    this.wallColour = player.getColour();
    this.joinColour = this.wallColour.darker();
  }

  /**
   * Gets the position of the city center relative to the origin
   *
   * @return position of the city center
   */
  @Override
  public Point2D getPosition() {
    return getCenter().getHexagon().getCenter();
  }

  /**
   * Determines if two cities are in the same position
   *
   * @param c other city to check
   * @return if the city centers are equal
   */
  public boolean sameCenterAs(City c) {
    return getCenter().samePositionAs(c.getCenter());
  }
}
