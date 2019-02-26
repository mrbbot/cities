package com.mrbbot.civilisation.logic.unit;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.geometry.Positionable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import javafx.geometry.Point2D;

import java.util.*;

import static com.mrbbot.civilisation.logic.unit.UnitAbility.ABILITY_ATTACK;
import static com.mrbbot.civilisation.logic.unit.UnitAbility.ABILITY_RANGED_ATTACK;

/**
 * Class representing an instance of a unit in the game.
 */
public class Unit extends Living implements Positionable {
  /**
   * Random number generator for generating improvement metadata
   */
  private static final Random RANDOM = new Random();

  /**
   * The owner of this unit. Only the owner can move the unit, or perform an action with it.
   */
  public Player player;
  /**
   * The tile this unit is currently occupying
   */
  public Tile tile;
  /**
   * The type of this unit. Contains information on the units abilities.
   */
  public UnitType unitType;

  /**
   * The number of movement points this unit has remaining this turn. When a unit moves, the cost of the movement is
   * taken away from this number. The unit can't move if this value is 0.
   */
  public int remainingMovementPointsThisTurn;
  /**
   * Whether or not the unit has attacked a living object this turn. Each attacking unit can only attack one unit per
   * turn, so this ensures that when it's set to true no more attacking can take place.
   */
  public boolean hasAttackedThisTurn;
  /**
   * What the unit is currently trying to build on the tile. Although this is part of the Unit class, this is only
   * relevant for units that have {@link UnitAbility#ABILITY_IMPROVE}.
   */
  public Improvement workerBuilding = Improvement.NONE;
  /**
   * How many turns the current unit has left on its build project. Again, this is only relevant for units that have
   * {@link UnitAbility#ABILITY_IMPROVE}.
   */
  public int workerBuildTurnsRemaining = 0;

  /**
   * Creates a new unit object with the specified data
   * @param player owner of the unit
   * @param tile tile the unit is occupying
   * @param unitType type of the unit
   */
  public Unit(Player player, Tile tile, UnitType unitType) {
    // Set required living parameters
    super(unitType.getBaseHealth());
    this.player = player;
    this.tile = tile;
    this.unitType = unitType;

    // Reset movement and attack state
    this.remainingMovementPointsThisTurn = unitType.getMovementPoints();
    this.hasAttackedThisTurn = false;

    // Check the tile doesn't already have a unit
    if (tile.unit != null) {
      throw new IllegalArgumentException("Unit created on tile with another unit");
    }
    tile.unit = this;
  }

  /**
   * Loads a unit from a map containing information about it
   * @param grid hexagon grid containing the unit
   * @param map map containing information on the unit
   */
  public Unit(HexagonGrid<Tile> grid, Map<String, Object> map) {
    // Load base health and health for living
    super((int) map.get("baseHealth"), (int) map.get("health"));

    // Load player
    this.player = new Player((String) map.get("owner"));

    // Load tile from the hexagon grid
    this.tile = grid.get((int) map.get("x"), (int) map.get("y"));

    // Load the unit type and check it exists
    this.unitType = UnitType.fromName((String) map.get("type"));
    assert this.unitType != null;

    // Load movement and attack state
    this.remainingMovementPointsThisTurn = (int) map.get("remainingMovementPoints");
    this.hasAttackedThisTurn = canAttack() && (boolean) map.get("hasAttacked");

    // Load specific worker information
    if (map.containsKey("workerBuilding"))
      workerBuilding = Improvement.fromName((String) map.get("workerBuilding"));
    if (map.containsKey("workerBuildTurnsRemaining"))
      workerBuildTurnsRemaining = (int) map.get("workerBuildTurnsRemaining");

    // Check the tile doesn't already have a unit
    if (tile.unit != null) {
      throw new IllegalArgumentException("Unit created on tile with another unit");
    }
    tile.unit = this;
  }

  /**
   * Gets the x-coordinate of this unit
   * @return x-coordinate of this unit
   */
  @Override
  public int getX() {
    return tile.x;
  }

  /**
   * Gets the y-coordinate of this unit
   * @return y-coordinate of this unit
   */
  @Override
  public int getY() {
    return tile.y;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> map = super.toMap();

    map.put("owner", player.id);
    map.put("x", tile.x);
    map.put("y", tile.y);
    map.put("type", unitType.getName());
    map.put("remainingMovementPoints", remainingMovementPointsThisTurn);
    if (canAttack()) map.put("hasAttacked", hasAttackedThisTurn);
    if (workerBuilding != Improvement.NONE)
      map.put("workerBuilding", workerBuilding.name);
    if (workerBuildTurnsRemaining != 0)
      map.put("workerBuildTurnsRemaining", workerBuildTurnsRemaining);

    return map;
  }

  public void startWorkerBuilding(Improvement improvement) {
    if (hasAbility(UnitAbility.ABILITY_IMPROVE)
      && workerBuilding == Improvement.NONE) {
      workerBuilding = improvement;
      workerBuildTurnsRemaining = improvement.turnCost;
    }
  }

  private int getAbilities() {
    int abilities = unitType.getAbilities();
    if (workerBuilding != Improvement.NONE) {
      abilities -= UnitAbility.ABILITY_MOVEMENT;
    }
    return abilities;
  }

  public boolean hasAbility(int ability) {
    return (getAbilities() & ability) > 0;
  }

  public boolean canAttack() {
    return hasAbility(ABILITY_ATTACK) || hasAbility(ABILITY_RANGED_ATTACK);
  }

  @Override
  public Tile[] handleTurn(Game game) {
    boolean tileUpdated = naturalHeal();
    boolean allTilesNeedReRendering = false;

    remainingMovementPointsThisTurn = unitType.getMovementPoints();
    hasAttackedThisTurn = false;

    if (workerBuilding != Improvement.NONE) {
      workerBuildTurnsRemaining--;
      if (workerBuildTurnsRemaining == 0) {
        HashMap<String, Object> meta = new HashMap<>();

        // check existing for road
        if (tile.improvement == Improvement.ROAD) allTilesNeedReRendering = true;
        if (workerBuilding == Improvement.CHOP_FOREST) {
          if (tile.city != null) tile.city.productionTotal += 30;
          tile.improvement = Improvement.NONE;
        } else {
          tile.improvement = workerBuilding;

          // Add metadata
          if (Improvement.FARM.equals(workerBuilding)) {
            meta.put("strips", ((RANDOM.nextInt(3) + 1) * 2) + 1);
            meta.put("angle", RANDOM.nextInt(6) * 60);
          } else if (Improvement.MINE.equals(workerBuilding)) {//Rocks
            List<Double> sizes = new ArrayList<>();
            List<Integer> colours = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
              sizes.add(RANDOM.nextDouble() / 3.0 + 0.5);
              colours.add(RANDOM.nextInt(3));
            }
            meta.put("sizes", sizes);
            meta.put("colours", colours);
          } else if (Improvement.ROAD.equals(workerBuilding)) {
            allTilesNeedReRendering = true;
          }
        }

        tile.improvementMetadata = meta;
        workerBuilding = Improvement.NONE;
      }
      tileUpdated = true;
    }

    return allTilesNeedReRendering
      ? new Tile[]{}
      : (tileUpdated
      ? new Tile[]{tile}
      : null);
  }

  @Override
  public void onAttack(Unit attacker, boolean ranged) {
    damage(attacker.unitType.getAttackStrength());
    if (!ranged) {
      attacker.damage(unitType.getBaseHealth() / 5);
    }
  }

  @Override
  public Player getOwner() {
    return player;
  }

  @Override
  public Point2D getPosition() {
    return tile.getHexagon().getCenter();
  }
}
