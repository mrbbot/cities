package com.mrbbot.civilisation.logic.map.tile;

public enum Improvement {
  NONE(
    0x00,
    "None",
    0,
    0,
    0,
    false
  ),
  CAPITAL(
    0x00,
    "Capital",
    0,
    0,
    0,
    false
  ),
  FARM(
    0x10,
    "Farm",
    2,
    2,
    0,
    true
  ),
  CHOP_FOREST(
    0x11,
    "Chop Forest",
    3,
    0,
    0,
    true
  ),
  MINE(
    0x12,
    "Mine",
    2,
    0,
    15,
    true
  ),
  PASTURE(
    0x13,
    "Pasture",
    4,
    4,
    0,
    true
  ),
  ROAD(
    0x14,
    "Road",
    2,
    0,
    0,
    true
  );

  public int unlockId;
  public String name;
  public int turnCost;
  public int foodPerTurn;
  public int productionPerTurn;
  public boolean workerCanDo;

  Improvement(
    int unlockId,
    String name,
    int turnCost,
    int foodPerTurn,
    int productionPerTurn,
    boolean workerCanDo
  ) {
    this.unlockId = unlockId;
    this.name = name;
    this.turnCost = turnCost;
    this.foodPerTurn = foodPerTurn;
    this.productionPerTurn = productionPerTurn;
    this.workerCanDo = workerCanDo;
  }
}
