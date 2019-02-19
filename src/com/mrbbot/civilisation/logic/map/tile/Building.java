package com.mrbbot.civilisation.logic.map.tile;

public enum Building {
  WALL(
    0x30,
    "Walls",
    "Description",
    100,
    0,
    1,
    1,
    0,
    1,
    100,
    1
  ),
  MONUMENT(
    0x31,
    "Monument",
    "Description",
    100,
    5,
    1,
    0.75,
    0,
    1,
    0,
    1
  ),
  BANK(
    0x32,
    "Bank",
    "Description",
    150,
    0,
    2,
    1,
    0,
    1,
    0,
    1
  ),
  AMPHITHEATRE(
    0x33,
    "Amphitheatre",
    "Description",
    150,
    10,
    1,
    1,
    0,
    1,
    0,
    1
  ),
  SCHOOL(
    0x34,
    "School",
    "Description",
    100,
    0,
    1,
    1,
    10,
    1,
    0,
    1
  ),
  UNIVERSITY(
    0x35,
    "University",
    "Description",
    200,
    0,
    1,
    1,
    0,
    2,
    0,
    1
  ),
  FACTORY(
    0x36,
    "Factory",
    "Description",
    200,
    0,
    1,
    1,
    0,
    1,
    0,
    2
  );

  public int unlockId;
  public String name;
  public String description;
  public int productionCost;

  public int goldPerTurn;
  public double goldPerTurnMultiplier;
  public double expansionCostMultiplier;

  public int sciencePerTurn;
  public double sciencePerTurnMultiplier;

  public int baseHealthIncrease;

  public double productionPerTurnMultiplier;

  Building(
    int unlockId,
    String name,
    String description,
    int productionCost,
    int goldPerTurn,
    double goldPerTurnMultiplier,
    double expansionCostMultiplier,
    int sciencePerTurn,
    double sciencePerTurnMultiplier,
    int baseHealthIncrease,
    double productionPerTurnMultiplier
  ) {
    this.unlockId = unlockId;
    this.name = name;
    this.description = description;
    this.productionCost = productionCost;
    this.goldPerTurn = goldPerTurn;
    this.goldPerTurnMultiplier = goldPerTurnMultiplier;
    this.expansionCostMultiplier = expansionCostMultiplier;
    this.sciencePerTurn = sciencePerTurn;
    this.sciencePerTurnMultiplier = sciencePerTurnMultiplier;
    this.baseHealthIncrease = baseHealthIncrease;
    this.productionPerTurnMultiplier = productionPerTurnMultiplier;

    assert goldPerTurnMultiplier != 0;
    assert expansionCostMultiplier != 0;
    assert sciencePerTurnMultiplier != 0;
    assert productionPerTurnMultiplier != 0;
  }
}
