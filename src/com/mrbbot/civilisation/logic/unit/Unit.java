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
   * The owner of this unit. Only the owner can move the unit, or perform an
   * action with it.
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
   * The number of movement points this unit has remaining this turn. When a
   * unit moves, the cost of the movement is taken away from this number. The
   * unit can't move if this value is 0.
   */
  public int remainingMovementPointsThisTurn;
  /**
   * Whether or not the unit has attacked a living object this turn. Each
   * attacking unit can only attack one unit per turn, so this ensures that
   * when it's set to true no more attacking can take place.
   */
  public boolean hasAttackedThisTurn;
  /**
   * What the unit is currently trying to build on the tile. Although this is
   * part of the Unit class, this is only relevant for units that have
   * {@link UnitAbility#ABILITY_IMPROVE}.
   */
  public Improvement workerBuilding = Improvement.NONE;
  /**
   * How many turns the current unit has left on its build project. Again, this
   * is only relevant for units that have {@link UnitAbility#ABILITY_IMPROVE}.
   */
  public int workerBuildTurnsRemaining = 0;

  /**
   * Creates a new unit object with the specified data
   *
   * @param player   owner of the unit
   * @param tile     tile the unit is occupying
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
      throw new IllegalArgumentException(
        "Unit created on tile with another unit"
      );
    }
    tile.unit = this;
  }

  /**
   * Loads a unit from a map containing information about it
   *
   * @param grid hexagon grid containing the unit
   * @param map  map containing information on the unit
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
    this.remainingMovementPointsThisTurn =
      (int) map.get("remainingMovementPoints");
    this.hasAttackedThisTurn = canAttack()
      && (boolean) map.get("hasAttacked");

    // Load specific worker information
    if (map.containsKey("workerBuilding"))
      workerBuilding =
        Improvement.fromName((String) map.get("workerBuilding"));
    if (map.containsKey("workerBuildTurnsRemaining"))
      workerBuildTurnsRemaining =
        (int) map.get("workerBuildTurnsRemaining");

    // Check the tile doesn't already have a unit
    if (tile.unit != null) {
      throw new IllegalArgumentException(
        "Unit created on tile with another unit"
      );
    }
    tile.unit = this;
  }

  /**
   * Gets the x-coordinate of this unit
   *
   * @return x-coordinate of this unit
   */
  @Override
  public int getX() {
    return tile.x;
  }

  /**
   * Gets the y-coordinate of this unit
   *
   * @return y-coordinate of this unit
   */
  @Override
  public int getY() {
    return tile.y;
  }

  /**
   * Stores all the information required to recreate this unit in map
   *
   * @return map containing unit information
   */
  @Override
  public Map<String, Object> toMap() {
    // Store health data from living base class
    Map<String, Object> map = super.toMap();

    // Store the owner
    map.put("owner", player.id);

    // Store the location
    map.put("x", tile.x);
    map.put("y", tile.y);

    // Store the unit type
    map.put("type", unitType.getName());

    // Store unit specific information
    map.put("remainingMovementPoints", remainingMovementPointsThisTurn);
    if (canAttack()) map.put("hasAttacked", hasAttackedThisTurn);
    if (workerBuilding != Improvement.NONE)
      map.put("workerBuilding", workerBuilding.name);
    if (workerBuildTurnsRemaining != 0)
      map.put("workerBuildTurnsRemaining", workerBuildTurnsRemaining);

    return map;
  }

  /**
   * Requests that the unit (if it can) begin working on constructing the
   * specified improvement on it's current tile
   *
   * @param improvement improvement to build
   */
  public void startWorkerBuilding(Improvement improvement) {
    // Check the unit can build and isn't already building something
    if (hasAbility(UnitAbility.ABILITY_IMPROVE)
      && workerBuilding == Improvement.NONE) {
      // Update the building improvement and turns remaining
      workerBuilding = improvement;
      workerBuildTurnsRemaining = improvement.turnCost;
    }
  }

  /**
   * Gets a units abilities. Normally, this should be the same of the unit
   * type's abilities. However, when a worker is building something, it
   * shouldn't be able to move.
   *
   * @return number representing a workers abilities
   */
  private int getAbilities() {
    int abilities = unitType.getAbilities();
    // Check if the worker is building something, and prevent it from moving
    // if it is
    if (workerBuilding != Improvement.NONE) {
      abilities -= UnitAbility.ABILITY_MOVEMENT;
    }
    return abilities;
  }

  /**
   * Check if a unit has the specified ability. This should be one of the
   * constants in the {@link UnitAbility} class. See {@link UnitAbility} for
   * more details on how this works.
   *
   * @param ability ability to check if the unit has
   * @return whether or not the unit has the ability
   */
  public boolean hasAbility(int ability) {
    return (getAbilities() & ability) > 0;
  }

  /**
   * Check if the unit can attack a living object
   *
   * @return whether the unit can perform a melee or a ranged attack
   */
  public boolean canAttack() {
    return hasAbility(ABILITY_ATTACK) || hasAbility(ABILITY_RANGED_ATTACK);
  }

  /**
   * Handle a turn of the game. This resets a unit's movement counter and
   * attack state, whilst also progressing any improvement that's being built.
   *
   * @param game game to handle the turn of
   * @return an array of tiles to be updated containing the unit's tile, an
   * empty array if all tiles should be updated, or null if no tiles should be
   * updated.
   */
  @Override
  public Tile[] handleTurn(Game game) {
    // Naturally heal the unit and see if the tile now needs to be updated
    boolean tileUpdated = naturalHeal();
    boolean allTilesNeedReRendering = false;

    // Reset movement and attack state
    remainingMovementPointsThisTurn = unitType.getMovementPoints();
    hasAttackedThisTurn = false;

    // Check if the unit is building something
    if (workerBuilding != Improvement.NONE) {
      // Increase build progress
      workerBuildTurnsRemaining--;
      // Check if the improvement has now been built
      if (workerBuildTurnsRemaining == 0) {
        // Create a new map for metadata
        HashMap<String, Object> meta = new HashMap<>();

        // Check if the improvement used to be a road. If it did, all tiles
        // will need re-rendering to recalculate road adjacencies.
        if (tile.improvement == Improvement.ROAD)
          allTilesNeedReRendering = true;
        if (workerBuilding == Improvement.CHOP_FOREST) {
          // If the working was chopping a forest, add the production bonus to
          // the cities total.
          if (tile.city != null) tile.city.productionTotal += 30;
          tile.improvement = Improvement.NONE;
        } else {
          // Otherwise just set the tiles improvement to what was being built
          tile.improvement = workerBuilding;

          // Check if there is any metadata that should be added
          if (Improvement.FARM.equals(workerBuilding)) {
            // Generate the number of strips and the angle for the farm
            meta.put("strips", ((RANDOM.nextInt(3) + 1) * 2) + 1);
            meta.put("angle", RANDOM.nextInt(6) * 60);
          } else if (Improvement.MINE.equals(workerBuilding)) {
            // Generate the size/colour for each of the 3 rocks
            List<Double> sizes = new ArrayList<>();
            List<Integer> colours = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
              sizes.add(RANDOM.nextDouble() / 3.0 + 0.5);
              colours.add(RANDOM.nextInt(3));
            }
            meta.put("sizes", sizes);
            meta.put("colours", colours);
          } else if (Improvement.ROAD.equals(workerBuilding)) {
            // Again if we're building a road, all tiles need to be updated to
            // recalculate road adjacencies
            allTilesNeedReRendering = true;
          }
        }

        tile.improvementMetadata = meta;
        // Reset the worker's building state
        workerBuilding = Improvement.NONE;
      }
      // Make sure the unit's tile is updated
      tileUpdated = true;
    }

    // Return an empty array if all tiles need to be updated
    return allTilesNeedReRendering
      ? new Tile[]{}
      : (tileUpdated
      ? new Tile[]{tile}
      : null);
  }

  /**
   * Handle another unit attacking this unit
   *
   * @param attacker the other unit attacking this unit
   * @param ranged   whether this was a ranged attack
   */
  @Override
  public void onAttacked(Unit attacker, boolean ranged) {
    // Damage the unit an amount based on the attacker's strength
    damage(attacker.unitType.getAttackStrength());
    if (!ranged) {
      // Only damage the attack if this wasn't a ranged attack
      attacker.damage(unitType.getBaseHealth() / 5);
    }
  }

  /**
   * Gets the owner of the unit
   *
   * @return owner of the unit
   */
  @Override
  public Player getOwner() {
    return player;
  }

  /**
   * Gets the position of the unit from the tile the unit is occupying
   *
   * @return position of the unit
   */
  @Override
  public Point2D getPosition() {
    return tile.getHexagon().getCenter();
  }
}
