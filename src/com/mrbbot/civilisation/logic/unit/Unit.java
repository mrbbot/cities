package com.mrbbot.civilisation.logic.unit;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.interfaces.Positionable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Tile;

import java.util.Map;

public class Unit extends Living implements Positionable {
  public Player player;
  public Tile tile;
  public UnitType unitType;

  public int remainingMovementPointsThisTurn;
  public boolean hasAttackedThisTurn;

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

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> map = super.toMap();

    map.put("owner", player.id);
    map.put("x", tile.x);
    map.put("y", tile.y);
    map.put("type", unitType.getName());
    map.put("remainingMovementPoints", remainingMovementPointsThisTurn);
    map.put("hasAttacked", hasAttackedThisTurn);

    return map;
  }

  @Override
  public Tile[] handleTurn(Game game) {
    remainingMovementPointsThisTurn = unitType.getMovementPoints();
    hasAttackedThisTurn = false;
    if (health < baseHealth) {
      health += 5;
      if (health > baseHealth) health = baseHealth;
      return new Tile[]{tile};
    }
    return null;
  }
}
