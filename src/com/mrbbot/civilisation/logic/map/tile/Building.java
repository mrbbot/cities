package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.ui.game.BadgeType;

import java.util.ArrayList;

public abstract class Building extends CityBuildable {
  public static int BASE_UNLOCK_ID = 0x30;

  public static Building WALL = new Building(
    "Walls",
    "Protect a city",
    100,
    BASE_UNLOCK_ID
  ) {
    @Override
    protected void setDetails() {
      baseHealthIncrease = 100;
    }
  };
  public static Building MONUMENT = new Building(
    "Monument",
    "Reduces cost of expansion",
    100,
    BASE_UNLOCK_ID + 1
  ) {
    @Override
    protected void setDetails() {
      goldPerTurnIncrease = 5;
      expansionCostMultiplier = 0.75;
    }
  };
  public static Building BANK = new Building(
    "Bank",
    "Doubles a city's gold per turn",
    150,
    BASE_UNLOCK_ID + 2
  ) {
    @Override
    protected void setDetails() {
      goldPerTurnMultiplier = 2;
    }
  };
  public static Building AMPHITHEATRE = new Building(
    "Amphitheatre",
    "Gives citizens a place to spend money",
    150,
    BASE_UNLOCK_ID + 3
  ) {
    @Override
    protected void setDetails() {
      goldPerTurnIncrease = 10;
    }
  };
  public static Building SCHOOL = new Building(
    "School",
    "Educates citizens",
    100,
    BASE_UNLOCK_ID + 4
  ) {
    @Override
    protected void setDetails() {
      sciencePerTurnIncrease = 10;
    }
  };
  public static Building UNIVERSITY = new Building(
    "University",
    "Must have a school in every city.",
    200,
    BASE_UNLOCK_ID + 5
  ) {
    @Override
    protected void setDetails() {
      sciencePerTurnMultiplier = 2;
    }

    @Override
    public String canBuildGivenCities(City city, ArrayList<City> cities) {
      String superReason = super.canBuildGivenCities(city, cities);
      if(superReason.length() > 0) return superReason;
      for (City otherCity : cities) {
        if(!otherCity.buildings.contains(SCHOOL)) {
          return "You must have a school in all of your cities!";
        }
      }
      return "";
    }
  };
  public static Building FACTORY = new Building(
    "Factory",
    "Increases cities production",
    200,
    BASE_UNLOCK_ID + 6
  ) {
    @Override
    protected void setDetails() {
      productionPerTurnMultiplier = 2;
    }
  };
  public static Building POWER_STATION = new Building(
    "Power Station",
    "Increases cities production",
    400,
    BASE_UNLOCK_ID + 7
  ) {
    @Override
    protected void setDetails() {
      productionPerTurnMultiplier = 2;
    }
  };
  public static Building SUPERMARKET = new Building(
    "Supermarket",
    "Gives citizens a place to get food",
    300,
    BASE_UNLOCK_ID + 8
  ) {
    @Override
    protected void setDetails() {
      goldPerTurnIncrease = 10;
      foodPerTurnMultiplier = 2;
    }
  };

  public static Building[] VALUES = new Building[]{
    WALL,
    MONUMENT,
    BANK,
    AMPHITHEATRE,
    SCHOOL,
    UNIVERSITY,
    FACTORY,
    POWER_STATION,
    SUPERMARKET
  };

  public static Building fromName(String name) {
    for (Building value : VALUES) {
      if(value.name.equals(name)) return value;
    }
    return null;
  }

  public int goldPerTurnIncrease = 0;
  public int sciencePerTurnIncrease = 0;
  public int baseHealthIncrease = 0;

  public double goldPerTurnMultiplier = 1;
  public double expansionCostMultiplier = 1;
  public double sciencePerTurnMultiplier = 1;
  public double productionPerTurnMultiplier = 1;
  public double foodPerTurnMultiplier = 1;

  private Building(String name, String description, int productionCost, int unlockId) {
    super(name, description, productionCost, unlockId);
    setDetails();
  }

  protected abstract void setDetails();

  private String getDetailTextForIncreaseWithMultiplier(int increase, double multiplier) {
    StringBuilder text = new StringBuilder();
    if(increase > 0) text.append(increase);
    if(multiplier != 1) {
      boolean increased = text.length() > 0;
      if(increased) text.append(" (");
      text.append("x").append((int)multiplier);
      if(increased) text.append(")");
    }
    return text.toString();
  }

  @Override
  public ArrayList<Detail> getDetails() {
    ArrayList<Detail> details = super.getDetails();

    String goldText = getDetailTextForIncreaseWithMultiplier(goldPerTurnIncrease, goldPerTurnMultiplier);
    if(goldText.length() > 0) details.add(new Detail(BadgeType.GOLD, goldText));

    String scienceText = getDetailTextForIncreaseWithMultiplier(sciencePerTurnIncrease, sciencePerTurnMultiplier);
    if(scienceText.length() > 0) details.add(new Detail(BadgeType.SCIENCE, scienceText));

    if(productionPerTurnMultiplier != 1) {
      details.add(new Detail(BadgeType.PRODUCTION, String.format("x%d", (int)productionPerTurnMultiplier)));
    }

    if(baseHealthIncrease != 0) {
      details.add(new Detail(BadgeType.HEALTH, baseHealthIncrease));
    }

    return details;
  }

  @Override
  public String canBuildGivenCities(City city, ArrayList<City> cities) {
    return city.buildings.contains(this) ? "You can only have one of these buildings per city" : "";
  }

  @Override
  public Tile build(City city, Game game) {
    city.buildings.add(this);
    return city.getCenter();
  }
}
