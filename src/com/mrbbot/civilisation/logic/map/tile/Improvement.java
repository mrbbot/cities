package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.logic.techs.Unlockable;

public class Improvement implements Unlockable {
  public static int BASE_UNLOCK_ID = 0x10;

  public static Improvement NONE = new Improvement(
    0x00,
    "None",
    0,
    0,
    0,
    false
  );
  public static Improvement CAPITAL = new Improvement(
    0x00,
    "Capital",
    0,
    0,
    0,
    false
  );
  public static Improvement TREE = new Improvement(
    0x00,
    "Tree",
    0,
    1,
    0,
    false
  );
  public static Improvement FARM = new Improvement(
    BASE_UNLOCK_ID,
    "Farm",
    2,
    2,
    0,
    true
  );
  public static Improvement CHOP_FOREST = new Improvement(
    BASE_UNLOCK_ID + 1,
    "Chop Forest",
    3,
    0,
    0,
    false
  );
  public static Improvement MINE = new Improvement(
    BASE_UNLOCK_ID + 2,
    "Mine",
    2,
    0,
    15,
    true
  );
  public static Improvement PASTURE = new Improvement(
    BASE_UNLOCK_ID + 3,
    "Pasture",
    4,
    4,
    0,
    true
  );
  public static Improvement ROAD = new Improvement(
    BASE_UNLOCK_ID + 4,
    "Road",
    2,
    0,
    0,
    true
  );

  public static Improvement[] VALUES = new Improvement[]{
    NONE,
    CAPITAL,
    TREE,
    FARM,
    CHOP_FOREST,
    MINE,
    PASTURE,
    ROAD
  };

  public static Improvement fromName(String name) {
    for (Improvement value : VALUES) {
      if(value.name.equals(name)) return value;
    }
    return null;
  }

  public int unlockId;
  public String name;
  public int turnCost;
  public int foodPerTurn;
  public int productionPerTurn;
  public boolean workerCanDo;

  private Improvement(
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

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Improvement) {
      return name.equals(((Improvement) obj).name);
    }
    return false;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getUnlockId() {
    return unlockId;
  }
}
