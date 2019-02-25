package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.ui.game.BadgeType;

import java.util.ArrayList;

/**
 * Class representing a building that can be built within a city. Declared abstract as buildings must implement
 * {@link Building#setDetails()} to register the abilities each building has.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Building extends CityBuildable {
  /**
   * Base unlock ID for buildings. Used to identify buildings that can be unlocked.
   */
  private static int BASE_UNLOCK_ID = 0x30;

  /*
   * START BUILDING DEFINITIONS
   */
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
      // Check if there is a reason why this can't be built already and return it if there is
      String superReason = super.canBuildGivenCities(city, cities);
      if (superReason.length() > 0) return superReason;

      // Otherwise check all other cities contain a school
      for (City otherCity : cities) {
        if (!otherCity.buildings.contains(SCHOOL)) {
          return "You must have a school in all of your cities!";
        }
      }

      // If they do, return an empty string indicating this building can be built
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
  /*
   * END BUILDING DEFINITIONS
   */

  /**
   * Array containing all defined buildings.
   */
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

  /**
   * Function to get a building from just its name
   *
   * @param name name of building to get
   * @return the building with the specified name or null if the building doesn't exist
   */
  public static Building fromName(String name) {
    // Iterates through all the buildings...
    for (Building value : VALUES) {
      // Checking if the names match
      if (value.name.equals(name)) return value;
    }
    return null;
  }

  /**
   * Increase in gold per turn for a city containing this building
   */
  public int goldPerTurnIncrease = 0;
  /**
   * Increase in science per turn for a city containing this building
   */
  public int sciencePerTurnIncrease = 0;
  /**
   * Increase in base health for a city containing this building
   */
  public int baseHealthIncrease = 0;

  /**
   * Gold per turn multiplier for a city containing the building
   */
  public double goldPerTurnMultiplier = 1;
  /**
   * Expansion cost multiplier for a city containing the building
   */
  public double expansionCostMultiplier = 1;
  /**
   * Science per turn multiplier for a city containing the building
   */
  public double sciencePerTurnMultiplier = 1;
  /**
   * Production per turn multiplier for a city containing the building
   */
  public double productionPerTurnMultiplier = 1;
  /**
   * Food per turn multiplier for a city containing the building
   */
  public double foodPerTurnMultiplier = 1;

  private Building(String name, String description, int productionCost, int unlockId) {
    // Pass required values to CityBuildable constructor
    super(name, description, productionCost, unlockId);
    setDetails();
  }

  /**
   * Called by the constructor to set the increases/multipliers this building provides for the city it's built in
   */
  protected abstract void setDetails();

  /**
   * Get the text to be displayed in the city production list for a resource that may have an increase and/or a
   * multiplier
   *
   * @param increase increase in resource this building provides
   * @param multiplier multiplier in resource this building provides
   * @return text to be displayed in the city production list, example "7 (x3)"
   */
  private String getDetailTextForIncreaseWithMultiplier(int increase, double multiplier) {
    StringBuilder text = new StringBuilder();
    // If there is an increase, add it to the text
    if (increase > 0) text.append(increase);
    // If there is a multiplier...
    if (multiplier != 1) {
      // Determine whether there was an increase
      boolean increased = text.length() > 0;
      // If there was, add a space and a bracket
      if (increased) text.append(" (");
      // Even if there wasn't add the multiplier
      text.append("x").append((int) multiplier);
      // Add the closing bracket if required
      if (increased) text.append(")");
    }
    return text.toString();
  }

  /**
   * Gets the details to be displayed in the city production list for this building
   * @return details to be displayed
   */
  @Override
  public ArrayList<Detail> getDetails() {
    // Get the details required for all CityBuildables (production/gold cost)
    ArrayList<Detail> details = super.getDetails();

    // Add the gold increase/multiplier (if there is one)
    String goldText = getDetailTextForIncreaseWithMultiplier(goldPerTurnIncrease, goldPerTurnMultiplier);
    if (goldText.length() > 0) details.add(new Detail(BadgeType.GOLD, goldText));

    // Add the science increase/multiplier (if there is one)
    String scienceText = getDetailTextForIncreaseWithMultiplier(sciencePerTurnIncrease, sciencePerTurnMultiplier);
    if (scienceText.length() > 0) details.add(new Detail(BadgeType.SCIENCE, scienceText));

    // Add the production multiplier (if there is one)
    if (productionPerTurnMultiplier != 1) {
      details.add(new Detail(BadgeType.PRODUCTION, String.format("x%d", (int) productionPerTurnMultiplier)));
    }

    // Add the science base health increase (if there is one)
    if (baseHealthIncrease != 0) {
      details.add(new Detail(BadgeType.HEALTH, baseHealthIncrease));
    }

    return details;
  }

  /**
   * Determine if a building can be built in a city given the player's other cities
   * @param city target city to build in
   * @param cities player's other cities
   * @return reason why the building cannot be built, or an empty string if it can
   */
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
