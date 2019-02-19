package com.mrbbot.civilisation.logic.unit;

import javafx.scene.paint.Color;

public enum UnitType {
  SETTLER(
    0x00,
    "Settler",
    "Description",
    Color.GOLD,
    1,
    0,
    5,
    60,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_SETTLE
  ),
  SCOUT(
    0x00,
    "Scout",
    "Description",
    Color.GREEN,
    4,
    5,
    25,
    40,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_ATTACK
  ),
  WARRIOR(
    0x00,
    "Warrior",
    "Description",
    Color.RED,
    2,
    10,
    50,
    50,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_ATTACK
  ),
  ARCHER(
    0x20,
    "Archer",
    "Description",
    Color.INDIANRED,
    2,
    5,
    30,
    60,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_RANGED_ATTACK
  ),
  WORKER(
    0x00,
    "Worker",
    "Description",
    Color.DODGERBLUE,
    3,
    0,
    15,
    40,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_IMPROVE
  ),
  ROCKET(
    0x21,
    "Rocket",
    "Description",
    Color.GREY,
    0,
    0,
    100,
    200,
    UnitAbility.ABILITY_BLAST_OFF
  );

  public int unlockId;
  public String name;
  public String description;
  public Color color;
  public int movementPoints;
  public int attackStrength;
  public int baseHealth;
  public int productionCost;
  public int abilities;

  UnitType(
    int unlockId,
    String name,
    String description,
    Color color,
    int movementPoints,
    int attackStrength,
    int baseHealth,
    int productionCost,
    int abilities
  ) {
    this.unlockId = unlockId;
    this.name = name;
    this.description = description;
    this.color = color;
    this.movementPoints = movementPoints;
    this.attackStrength = attackStrength;
    this.baseHealth = baseHealth;
    this.productionCost = productionCost;
    this.abilities = abilities;
  }
}
