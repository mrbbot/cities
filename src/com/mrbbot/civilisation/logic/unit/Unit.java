package com.mrbbot.civilisation.logic.unit;

import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.interfaces.Positionable;
import com.mrbbot.civilisation.logic.map.tile.Tile;

import java.io.Serializable;

public class Unit extends Living implements Positionable {
  public Player player;
  public Tile tile;
  public UnitType unitType;

  public Unit(Player player, Tile tile, UnitType unitType) {
    super(unitType.baseHealth);
    this.player = player;
    this.tile = tile;
    this.unitType = unitType;
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
}
