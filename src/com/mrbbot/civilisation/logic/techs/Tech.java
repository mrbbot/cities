package com.mrbbot.civilisation.logic.techs;

import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.unit.UnitType;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Set;

public class Tech {
  private static ArrayList<Tech> REGISTRY = new ArrayList<>();
  public static int MAX_X = 0;

  static {
    // Definitions: names, positions and colours
    Tech civilisation = new Tech("Civilisation", 0, 0, Color.GOLDENROD);

    Tech agriculture = new Tech("Agriculture", 1, 0, Color.GREEN);

    Tech forestry = new Tech("Forestry", 2, 0, Color.DARKGREEN);

    Tech pottery = new Tech("Pottery", 3, -2, Color.FIREBRICK);
    Tech husbandry = new Tech("Husbandry", 3, -1, Color.PINK);
    Tech theWheel = new Tech("The Wheel", 3, 0, Color.GOLDENROD);
    Tech archery = new Tech("Archery", 3, 1, Color.RED);
    Tech mining = new Tech("Mining", 3, 2, Color.GREY);

    Tech currency = new Tech("Currency", 4, -2, Color.GOLD);
    Tech dramaAndPoetry = new Tech("Drama", 4, -1, Color.PURPLE);
    Tech ironWorking = new Tech("Iron Working", 4, 2, Color.GREY);

    Tech education = new Tech("Education", 5, 0, Color.LIGHTBLUE);

    Tech industrialisation = new Tech("Industrialisation", 6, -1, Color.BLACK);
    Tech steel = new Tech("Steel", 6, 1, Color.GREY.darker());

    Tech electricity = new Tech("Electricity", 7, -1, Color.GOLD);
    Tech plastics = new Tech("Plastics", 7, 1, Color.PINK);

    Tech rocketry = new Tech("Rocketry", 8, 0, Color.DARKBLUE);

    // Unlocks: improvements, buildings, unit types
    civilisation.unlocks(UnitType.SETTLER);
    civilisation.unlocks(UnitType.SCOUT);
    civilisation.unlocks(UnitType.WARRIOR);

    agriculture.unlocks(Improvement.FARM);
    agriculture.unlocks(UnitType.WORKER);

    forestry.unlocks(Improvement.CHOP_FOREST);

    pottery.unlocks(Building.MONUMENT);
    husbandry.unlocks(Improvement.PASTURE);
    theWheel.unlocks(Improvement.ROAD);
    archery.unlocks(UnitType.ARCHER);
    mining.unlocks(Improvement.MINE);
    mining.unlocks(Building.WALL);

    currency.unlocks(Building.BANK);
    dramaAndPoetry.unlocks(Building.AMPHITHEATRE);
    ironWorking.unlocks(UnitType.SWORDSMAN);

    education.unlocks(Building.SCHOOL);
    education.unlocks(Building.UNIVERSITY);

    industrialisation.unlocks(Building.FACTORY);
    steel.unlocks(UnitType.KNIGHT);

    electricity.unlocks(Building.POWER_STATION);
    plastics.unlocks(Building.SUPERMARKET);

    rocketry.unlocks(UnitType.ROCKET);

    // Previous tech requirements
    agriculture.requires(civilisation);

    forestry.requires(agriculture);

    pottery.requires(agriculture);
    husbandry.requires(forestry);
    theWheel.requires(forestry);
    archery.requires(forestry);
    mining.requires(agriculture);

    currency.requires(pottery);
    currency.requires(husbandry);
    dramaAndPoetry.requires(pottery);
    dramaAndPoetry.requires(husbandry);
    ironWorking.requires(archery);
    ironWorking.requires(mining);

    education.requires(dramaAndPoetry);
    education.requires(theWheel);

    industrialisation.requires(currency);
    industrialisation.requires(education);
    steel.requires(education);
    steel.requires(ironWorking);

    electricity.requires(industrialisation);
    electricity.requires(steel);
    plastics.requires(industrialisation);
    plastics.requires(steel);

    rocketry.requires(electricity);
    rocketry.requires(plastics);

    // Calculate tech costs
    for (Tech tech : Tech.REGISTRY) {
      // must be called in order, caches
      int cost = tech.getScienceCost();
      System.out.println(String.format("%s: %d", tech.getName(), cost));
      if (tech.getX() > MAX_X) MAX_X = tech.getX();
    }
  }

  public static Tech getRoot() {
    return REGISTRY.get(0);
  }

  public static Tech fromName(String name) {
    for (Tech tech : REGISTRY) {
      if (tech.name.equals(name)) return tech;
    }
    return null;
  }

  private final String name;
  private final int x, y;
  private final Color colour;
  private final ArrayList<Tech> requirements;
  private final ArrayList<Tech> requiredBy;
  private final ArrayList<Unlockable> unlocks;
  private int scienceCost = -1;

  private Tech(String name, int x, int y, Color colour) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.colour = colour;
    this.requirements = new ArrayList<>();
    this.requiredBy = new ArrayList<>();
    this.unlocks = new ArrayList<>();
    REGISTRY.add(this);
  }

  private void requires(Tech tech) {
    requirements.add(tech);
    tech.requiredBy.add(this);
  }

  private void unlocks(Unlockable unlockable) {
    unlocks.add(unlockable);
  }

  public String getName() {
    return name;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Color getColour() {
    return colour;
  }

  public ArrayList<Tech> getRequirements() {
    return requirements;
  }

  public ArrayList<Tech> getRequiredBy() {
    return requiredBy;
  }

  public ArrayList<Unlockable> getUnlocks() {
    return unlocks;
  }

  public int getScienceCost() {
    // We only want to calculate this once as it's recursive and could take a long time
    if (scienceCost == -1) {
      if (requirements.size() == 0) {
        scienceCost = 0;
      } else {
        scienceCost = requirements.stream().mapToInt(Tech::getScienceCost).sum() + 25;
      }
    }
    return scienceCost;
  }

  public boolean canUnlockGivenUnlocked(Set<Tech> unlockedTechs) {
    return unlockedTechs.containsAll(requirements) || (requirements.size() == 1 && requirements.get(0).requirements.size() == 0);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Tech) {
      return name.equals(((Tech) obj).name);
    }
    return false;
  }
}
