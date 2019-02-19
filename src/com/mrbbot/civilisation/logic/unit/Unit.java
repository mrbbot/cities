package com.mrbbot.civilisation.logic.unit;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.interfaces.Positionable;
import com.mrbbot.civilisation.logic.map.tile.Tile;

import java.util.Map;

public class Unit extends Living implements Positionable {
  public Player player;
  public Tile tile;
  public UnitType unitType;

  public int remainingMovementPointsThisTurn;
  public boolean hasAttackedThisTurn;

  public Unit(Player player, Tile tile, UnitType unitType) {
    super(unitType.baseHealth);
    this.player = player;
    this.tile = tile;
    this.unitType = unitType;
    this.remainingMovementPointsThisTurn = unitType.movementPoints;
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
    this.unitType = UnitType.valueOf((String) map.get("type"));
    this.remainingMovementPointsThisTurn = (int) map.get("remainingMovementPoints");
    this.hasAttackedThisTurn = (boolean) map.get("hasAttacked");
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

  public boolean hasAbility(int ability) {
    return (unitType.abilities & ability) > 0;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> map = super.toMap();

    map.put("owner", player.id);
    map.put("x", tile.x);
    map.put("y", tile.y);
    map.put("type", unitType.toString());
    map.put("remainingMovementPoints", remainingMovementPointsThisTurn);
    map.put("hasAttacked", hasAttackedThisTurn);

    return map;
  }
}
