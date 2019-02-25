package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.logic.techs.Unlockable;

/**
 * Class representing an improvement that can be built within a cities bounds. Most of these are built by workers.
 */
public class Improvement implements Unlockable {
  /**
   * Base unlock ID for improvements. Used to identify improvements that can be unlocked.
   */
  private static int BASE_UNLOCK_ID = 0x10;

  /*
   * START IMPROVEMENT DEFINITIONS
   */
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
  /*
   * END IMPROVEMENT DEFINITIONS
   */

  /**
   * Array containing all defined improvements
   */
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

  /**
   * Function to get an improvement from just its name
   *
   * @param name name of improvement to get
   * @return the improvement with the specified name or null if the improvement doesn't exist
   */
  public static Improvement fromName(String name) {
    // Iterates through all the improvements...
    for (Improvement value : VALUES) {
      // Check if the names match
      if (value.name.equals(name)) return value;
    }
    return null;
  }

  /**
   * Unlock ID used to identify an unlockable improvement
   */
  public int unlockId;
  /**
   * User friendly name of this improvement
   */
  public String name;
  /**
   * Number of turns a worker unit takes to build this improvement
   */
  public int turnCost;
  /**
   * Food per turn increase for a city containing the improvement
   */
  public int foodPerTurn;
  /**
   * Production per turn increase for a city containing the improvement
   */
  public int productionPerTurn;
  /**
   * Whether a worker can build this improvement
   */
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
    if (obj instanceof Improvement) {
      // Only check the names are equal, as these should be unique
      return name.equals(((Improvement) obj).name);
    }
    return false;
  }

  /**
   * Gets the user friendly name of this improvement
   *
   * @return user friendly name of this improvement
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Gets the unlock ID of this improvement
   *
   * @return unlock ID of this improvement
   */
  @Override
  public int getUnlockId() {
    return unlockId;
  }
}
