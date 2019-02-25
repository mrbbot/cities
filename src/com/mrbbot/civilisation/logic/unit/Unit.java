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
    if (tile.unit != null) {
      throw new IllegalArgumentException("Unit created on tile with another unit");
    }
    tile.unit = this;
  }

  public Unit(HexagonGrid<Tile> grid, Map<String, Object> map) {
    super((int) map.get("baseHealth"), (int) map.get("health"));
    this.player = new Player((String) map.get("owner"));
    this.tile = grid.get((int) map.get("x"), (int) map.get("y"));
    this.unitType = UnitType.fromName((String) map.get("type"));
    assert this.unitType != null;
    this.remainingMovementPointsThisTurn = (int) map.get("remainingMovementPoints");
    this.hasAttackedThisTurn = canAttack() && (boolean) map.get("hasAttacked");
    if(map.containsKey("workerBuilding"))
      workerBuilding = Improvement.fromName((String) map.get("workerBuilding"));
    if(map.containsKey("workerBuildTurnsRemaining"))
      workerBuildTurnsRemaining = (int) map.get("workerBuildTurnsRemaining");
    if (tile.unit != null) {
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
    if (canAttack()) map.put("hasAttacked", hasAttackedThisTurn);
    if(workerBuilding != Improvement.NONE)
      map.put("workerBuilding", workerBuilding.name);
    if(workerBuildTurnsRemaining != 0)
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
        if(tile.improvement == Improvement.ROAD) allTilesNeedReRendering = true;
        if (workerBuilding == Improvement.CHOP_FOREST) {
          if(tile.city != null) tile.city.productionTotal += 30;
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
    if(!ranged) {
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
