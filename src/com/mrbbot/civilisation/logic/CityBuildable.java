package com.mrbbot.civilisation.logic;

import com.mrbbot.civilisation.logic.interfaces.Unlockable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.ui.game.BadgeType;

import java.util.ArrayList;

public abstract class CityBuildable implements Unlockable {
  public static CityBuildable fromName(String name) {
    UnitType unitType = UnitType.fromName(name);
    if(unitType != null) return unitType;
    return Building.fromName(name);
  }

  public class Detail {
    public final BadgeType badge;
    public final String text;

    public Detail(BadgeType badge, String text) {
      this.badge = badge;
      this.text = text;
    }

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

  public final String getName() {
    return name;
  }

  public final int getUnlockId() {
    return unlockId;
  }

  public final String getDescription() {
    return description;
  }

  public final boolean canBuildWith(int productionTotal) {
    return productionTotal >= productionCost;
  }

  public ArrayList<Detail> getDetails() {
    ArrayList<Detail> details = new ArrayList<>();
    details.add(new Detail(BadgeType.PRODUCTION, productionCost));
    return details;
  }
  public abstract void build(Game game);
  public boolean canBuild(ArrayList<City> cities) {
    return true;
  }
}
