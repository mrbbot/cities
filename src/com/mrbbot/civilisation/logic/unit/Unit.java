package com.mrbbot.civilisation.logic.unit;

import com.mrbbot.civilisation.logic.interfaces.Positionable;
import com.mrbbot.civilisation.logic.map.tile.Tile;

import java.io.Serializable;

public class Unit implements Positionable, Serializable {
  public Tile tile;

  public Unit(Tile tile) {
    this.tile = tile;
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
}
