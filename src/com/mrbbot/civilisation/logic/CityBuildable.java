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

  protected final String name;
  protected final String description;
  protected final int productionCost;
  protected final int unlockId;

  public CityBuildable(String name, String description, int productionCost, int unlockId) {
    this.name = name;
    this.description = description;
    this.productionCost = productionCost;
    this.unlockId = unlockId;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CityBuildable) {
      return Objects.equals(name, ((CityBuildable) obj).name);
    }
    return false;
  }

  public final String getName() {
    return name;
  }

  public final int getUnlockId() {
    return unlockId;
  }

  public final String getDescription() {
    return description;
  }

  public final int getProductionCost() {
    return productionCost;
  }

  public final int getGoldCost() {
    return (int) Math.round(productionCost * 1.5);
  }

  public final boolean canBuildWithProduction(int productionTotal) {
    return productionTotal >= productionCost;
  }

  public final boolean canBuildWithGold(int goldTotal) {
    return goldTotal >= getGoldCost();
  }

  public ArrayList<Detail> getDetails() {
    ArrayList<Detail> details = new ArrayList<>();
    details.add(new Detail(BadgeType.PRODUCTION, productionCost));
    details.add(new Detail(BadgeType.GOLD, getGoldCost()));
    return details;
  }

  public abstract Tile build(City city, Game game);

  public String canBuildGivenCities(City city, ArrayList<City> cities) {
    return "";
  }
}
