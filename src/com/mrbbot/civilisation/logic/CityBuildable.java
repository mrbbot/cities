package com.mrbbot.civilisation.logic;

import com.mrbbot.civilisation.logic.techs.Unlockable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.ui.game.BadgeType;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Abstract class representing an item that can be built within a city by the
 * city instead of a worker
 */
public abstract class CityBuildable implements Unlockable {
  /**
   * Gets a city buildable from just its name
   *
   * @param name name of the city buildable to get
   * @return the city buildable with the specified name or null if it doesn't
   * exist
   */
  public static CityBuildable fromName(String name) {
    // Try get it as a unit type
    UnitType unitType = UnitType.fromName(name);
    if (unitType != null) return unitType;
    // Otherwise, try get it as a building (this will return null if it doesn't
    // exist)
    return Building.fromName(name);
  }

  /**
   * Class representing a detail in the city production list. This could be the
   * production cost, the amount of movement points, or something like the
   * gold per turn increase.
   */
  public class Detail {
    /**
     * Type of badge that should be used to represent this detail
     */
    public final BadgeType badge;
    /**
     * Text contents of this detail
     */
    public final String text;

    /**
     * Creates a new detail
     *
     * @param badge badge type of the detail
     * @param text  text to be shown next to the badge
     */
    public Detail(BadgeType badge, String text) {
      this.badge = badge;
      this.text = text;
    }

    /**
     * Creates a new detail with a number that is automatically converted to a
     * string
     *
     * @param badge  badge type of the detail
     * @param number number to be shown next to the badge
     */
    public Detail(BadgeType badge, int number) {
      this(badge, String.valueOf(number));
    }
  }

  /**
   * Name of this buildable (i.e. what is displayed in the city production
   * list)
   */
  protected final String name;
  /**
   * Description of this buildable (i.e. what is displayed in the city
   * production list)
   */
  protected final String description;
  /**
   * Amount of production points required to build this thing. Also determines
   * the gold cost (1.5x this value).
   */
  @SuppressWarnings("WeakerAccess")
  protected final int productionCost;
  /**
   * Unlock ID of this buildable. Used to track what things are unlocked by a
   * technology.
   */
  protected final int unlockId;

  public CityBuildable(
    String name,
    String description,
    int productionCost,
    int unlockId
  ) {
    this.name = name;
    this.description = description;
    this.productionCost = productionCost;
    this.unlockId = unlockId;
  }

  @Override
  public int hashCode() {
    // Name should be unique
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CityBuildable) {
      // Name should be unique
      return Objects.equals(name, ((CityBuildable) obj).name);
    }
    return false;
  }

  /**
   * Gets the name of the buildable to be displayed in the production list
   *
   * @return name of the buildable
   */
  public final String getName() {
    return name;
  }

  /**
   * Gets the unlock ID of the buildable
   *
   * @return unlock ID of the buildable
   */
  public final int getUnlockId() {
    return unlockId;
  }

  /**
   * Gets the description of the buildable to be displayed in the production
   * list
   *
   * @return description of the buildable
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Gets the required production total for this buildable
   *
   * @return production cost of this buildable
   */
  public final int getProductionCost() {
    return productionCost;
  }

  /**
   * Calculates the gold cost of this item from the production cost
   *
   * @return productionCost * 1.5
   */
  public final int getGoldCost() {
    return (int) Math.round(productionCost * 1.5);
  }

  /**
   * Checks if this buildable can be built with the player's current production
   * total
   *
   * @param productionTotal player's current production total to check against
   * @return whether the buildable can be built
   */
  public final boolean canBuildWithProduction(int productionTotal) {
    return productionTotal >= productionCost;
  }

  /**
   * Checks if this buildable can be built with the player's current gold total
   *
   * @param goldTotal player's current gold total to check against
   * @return whether the buildable can be built
   */
  public final boolean canBuildWithGold(int goldTotal) {
    return goldTotal >= getGoldCost();
  }

  /**
   * Gets the details to be displayed in the city production list for this
   * building. This should be overridden in subclasses to add more specific
   * details.
   *
   * @return details to be displayed
   */
  public ArrayList<Detail> getDetails() {
    ArrayList<Detail> details = new ArrayList<>();
    // Add cost details (common for all buildables)
    details.add(new Detail(BadgeType.PRODUCTION, productionCost));
    details.add(new Detail(BadgeType.GOLD, getGoldCost()));
    return details;
  }

  /**
   * Function that builds the buildable in the city. Must be overridden in
   * subclasses for actual implementation.
   *
   * @param city city to build the buildable in
   * @param game game containing the city
   * @return tile updated during the build process
   */
  public abstract Tile build(City city, Game game);

  /**
   * Determine if a buildable can be built in a city given the player's other
   * cities. Designed to be overridden in subclasses.
   *
   * @param city   target city to build in
   * @param cities player's other cities
   * @return reason why the buildable cannot be built, or an empty string if it
   * can
   */
  public String canBuildGivenCities(City city, ArrayList<City> cities) {
    return "";
  }
}
