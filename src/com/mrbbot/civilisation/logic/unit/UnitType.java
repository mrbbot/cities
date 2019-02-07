package com.mrbbot.civilisation.logic.unit;

import javafx.scene.paint.Color;

public enum UnitType {
  SETTLER(
    "Settler",
    Color.GOLD,
    1,
    0,
    5,
    60,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_SETTLE
  ),
  SCOUT(
    "Scout",
    Color.GREEN,
    4,
    5,
    25,
    40,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_ATTACK
  ),
  WARROR(
    "Warrior",
    Color.RED,
    2,
    10,
    50,
    50,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_ATTACK
  ),
  ARCHER(
    "Archer",
    Color.INDIANRED,
    2,
    5,
    30,
    60,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_RANGED_ATTACK
  ),
  WORKER(
    "Worker",
    Color.DODGERBLUE,
    3,
    0,
    15,
    40,
    UnitAbility.ABILITY_MOVEMENT + UnitAbility.ABILITY_IMPROVE
  ),
  ROCKET(
    "Rocket",
    Color.GREY,
    0,
    0,
    100,
    200,
    UnitAbility.ABILITY_BLAST_OFF
  );

  public String name;
  public Color color;
  public int movementPoints;
  public int attackStrength;
  public int baseHealth;
  public int cost;
  public int abilities;

  UnitType(
    String name,
    Color color,
    int movementPoints,
    int attackStrength,
    int baseHealth,
    int cost,
    int abilities
  ) {
    this.name = name;
    this.color = color;
    this.movementPoints = movementPoints;
    this.attackStrength = attackStrength;
    this.baseHealth = baseHealth;
    this.cost = cost;
    this.abilities = abilities;
  }
}
