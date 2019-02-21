package com.mrbbot.civilisation.logic.unit;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.interfaces.Positionable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.map.tile.Tile;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.mrbbot.civilisation.logic.unit.UnitAbility.ABILITY_ATTACK;
import static com.mrbbot.civilisation.logic.unit.UnitAbility.ABILITY_RANGED_ATTACK;

public class Unit extends Living implements Positionable {
  private static final Random RANDOM = new Random();

  public Player player;
  public Tile tile;
  public UnitType unitType;

  public int remainingMovementPointsThisTurn;
  public boolean hasAttackedThisTurn;
  public Improvement workerBuilding = Improvement.NONE;
  public int workerBuildTurnsRemaining = 0;

  public Unit(Player player, Tile tile, UnitType unitType) {
    super(unitType.getBaseHealth());
    this.player = player;
    this.tile = tile;
    this.unitType = unitType;
    this.remainingMovementPointsThisTurn = unitType.getMovementPoints();
    this.hasAttackedThisTurn = false;
    if(tile.unit != null) {
      throw new IllegalArgumentException("Unit created on tile with another unit");
    }
    tile.unit = this;
  }

  public Unit(HexagonGrid<Tile> grid, Map<String, Object> map) {
    super((Integer) map.get("baseHealth"));
    this.health = (int) map.get("health");
    this.player = new Player((String) map.get("owner"));
    this.tile = grid.get((int) map.get("x"), (int) map.get("y"));
    this.unitType = UnitType.fromName((String) map.get("type"));
    assert this.unitType != null;
    this.remainingMovementPointsThisTurn = (int) map.get("remainingMovementPoints");
    this.hasAttackedThisTurn = canAttack() && (boolean) map.get("hasAttacked");
    if(tile.unit != null) {
      throw new IllegalArgumentException("Unit created on tile with another unit");
    }
    tile.unit = this;
  }

  @Override
  public int getX() {
    return tile.x;
  }

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
    if(canAttack()) map.put("hasAttacked", hasAttackedThisTurn);

    return map;
  }

  public void startWorkerBuilding(Improvement improvement) {
    if(workerBuilding == Improvement.NONE) {
      workerBuilding = improvement;
      workerBuildTurnsRemaining = improvement.turnCost;
    }
  }

  private int getAbilities() {
    int abilities = unitType.getAbilities();
    if(workerBuilding != Improvement.NONE) {
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
    boolean workerTileUpdated = false;

    remainingMovementPointsThisTurn = unitType.getMovementPoints();
    hasAttackedThisTurn = false;
    if (health < baseHealth) {
      health += 5;
      if (health > baseHealth) health = baseHealth;
      workerTileUpdated = true;
    }

    if(workerBuilding != Improvement.NONE) {
      workerBuildTurnsRemaining--;
      if(workerBuildTurnsRemaining == 0) {
        tile.improvementMetadata = new HashMap<>();
        if(workerBuilding == Improvement.CHOP_FOREST) {
          tile.improvement = Improvement.NONE;
        } else {
          tile.improvement = workerBuilding;
          tile.improvementMetadata.put("strips", ((RANDOM.nextInt(3) + 1) * 2) + 1);
          tile.improvementMetadata.put("angle", RANDOM.nextInt(6) * 60);
        }
        workerBuilding = Improvement.NONE;
      }
      workerTileUpdated = true;
    }

    return workerTileUpdated ? new Tile[]{tile} : null;
  }
}
