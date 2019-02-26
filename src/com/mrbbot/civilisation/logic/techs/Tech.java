package com.mrbbot.civilisation.logic.techs;

import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.unit.UnitType;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Set;

/**
 * Class representing a technology that can be unlocked by a player. This class
 * also contains the static tech registry that contains a list of all the
 * available techs. These are all initialised in this class too.
 */
public class Tech {
  /**
   * List of the techs that have been created. The first item added to this
   * list should be the root of the tech tree.
   */
  private static ArrayList<Tech> REGISTRY = new ArrayList<>();
  /**
   * The maximum x-coordinate of a tech on the tech tree. This is used to work
   * out the width of the tech tree, so the scroll pane knows how long to
   * scroll for. See {@link com.mrbbot.civilisation.ui.game.UITechTree}.
   */
  public static int MAX_X = 0;

  /*
   * START TECH DEFINITIONS
   */
  static {
    // Create the initial definitions for each tech, these include the name,
    // position of the tech tree, and their colour.

    // Level 0 (unlocked by default)
    Tech civilisation =
      new Tech("Civilisation", 0, 0, Color.GOLDENROD);

    // Level 1
    Tech agriculture = new Tech("Agriculture", 1, 0, Color.GREEN);

    // Level 2
    Tech forestry = new Tech("Forestry", 2, 0, Color.DARKGREEN);

    // Level 3
    Tech pottery = new Tech("Pottery", 3, -2, Color.FIREBRICK);
    Tech husbandry = new Tech("Husbandry", 3, -1, Color.PINK);
    Tech theWheel = new Tech("The Wheel", 3, 0, Color.GOLDENROD);
    Tech archery = new Tech("Archery", 3, 1, Color.RED);
    Tech mining = new Tech("Mining", 3, 2, Color.GREY);

    // Level 4
    Tech currency = new Tech("Currency", 4, -2, Color.GOLD);
    Tech dramaAndPoetry = new Tech("Drama", 4, -1, Color.PURPLE);
    Tech ironWorking = new Tech("Iron Working", 4, 2, Color.GREY);

    // Level 5
    Tech education = new Tech("Education", 5, 0, Color.LIGHTBLUE);

    // Level 6
    Tech industrialisation =
      new Tech("Industrialisation", 6, -1, Color.BLACK);
    Tech steel = new Tech("Steel", 6, 1, Color.GREY.darker());

    // Level 7
    Tech electricity = new Tech("Electricity", 7, -1, Color.GOLD);
    Tech plastics = new Tech("Plastics", 7, 1, Color.PINK);

    // Level 8
    Tech rocketry = new Tech("Rocketry", 8, 0, Color.DARKBLUE);

    // Specify the Unlockables that each tech unlocks. These could be
    // improvements, buildings, or unit types.

    // Level 0 (unlocked by default)
    civilisation.unlocks(UnitType.SETTLER);
    civilisation.unlocks(UnitType.SCOUT);
    civilisation.unlocks(UnitType.WARRIOR);

    // Level 1
    agriculture.unlocks(Improvement.FARM);
    agriculture.unlocks(UnitType.WORKER);

    // Level 2
    forestry.unlocks(Improvement.CHOP_FOREST);

    // Level 3
    pottery.unlocks(Building.MONUMENT);
    husbandry.unlocks(Improvement.PASTURE);
    theWheel.unlocks(Improvement.ROAD);
    archery.unlocks(UnitType.ARCHER);
    mining.unlocks(Improvement.MINE);
    mining.unlocks(Building.WALL);

    // Level 4
    currency.unlocks(Building.BANK);
    dramaAndPoetry.unlocks(Building.AMPHITHEATRE);
    ironWorking.unlocks(UnitType.SWORDSMAN);

    // Level 5
    education.unlocks(Building.SCHOOL);
    education.unlocks(Building.UNIVERSITY);

    // Level 6
    industrialisation.unlocks(Building.FACTORY);
    steel.unlocks(UnitType.KNIGHT);

    // Level 7
    electricity.unlocks(Building.POWER_STATION);
    plastics.unlocks(Building.SUPERMARKET);

    // Level 8
    rocketry.unlocks(UnitType.ROCKET);

    // Specify the requirements for each of the techs. Techs can require
    // multiple techs, but these must all be on a previous level.

    // Level 0 techs have no requirements as they are unlocked by default.

    // Level 1
    agriculture.requires(civilisation);

    // Level 2
    forestry.requires(agriculture);

    // Level 3
    pottery.requires(agriculture);
    husbandry.requires(forestry);
    theWheel.requires(forestry);
    archery.requires(forestry);
    mining.requires(agriculture);

    // Level 4
    currency.requires(pottery);
    currency.requires(husbandry);
    dramaAndPoetry.requires(pottery);
    dramaAndPoetry.requires(husbandry);
    ironWorking.requires(archery);
    ironWorking.requires(mining);

    // Level 5
    education.requires(dramaAndPoetry);
    education.requires(theWheel);

    // Level 6
    industrialisation.requires(currency);
    industrialisation.requires(education);
    steel.requires(education);
    steel.requires(ironWorking);

    // Level 7
    electricity.requires(industrialisation);
    electricity.requires(steel);
    plastics.requires(industrialisation);
    plastics.requires(steel);

    // Level 8
    rocketry.requires(electricity);
    rocketry.requires(plastics);

    // Calculate tech science costs
    for (Tech tech : Tech.REGISTRY) {
      // Even though the costs are only printed, and not used for anything
      // else, this function must be called in level order as the value is
      // cached and previous requirements' tech costs are used in the
      // calculations of later techs.
      int cost = tech.getScienceCost();
      System.out.println(String.format("%s: %d", tech.getName(), cost));
      if (tech.getX() > MAX_X) MAX_X = tech.getX();
    }
  }
  /*
   * END TECH DEFINITIONS
   */

  /**
   * Gets the root of the tech tree. This is the first item that was added to
   * the registry.
   *
   * @return root of the tech tree
   */
  public static Tech getRoot() {
    return REGISTRY.get(0);
  }

  /**
   * Gets a tech from just its name. Used to load techs from a map (network/
   * file)
   *
   * @param name name of the tech to load
   * @return Tech object for the tech or null if the tech doesn't exist
   */
  public static Tech fromName(String name) {
    for (Tech tech : REGISTRY) {
      if (tech.name.equals(name)) return tech;
    }
    return null;
  }

  /**
   * The user facing name of the tech. Used by
   * {@link com.mrbbot.civilisation.ui.game.UITechTree} when displaying a tech.
   */
  private final String name;
  /**
   * The x-coordinate of the tech. Used by
   * {@link com.mrbbot.civilisation.ui.game.UITechTree} to position a tech in
   * the tree.
   */
  private final int x;
  /**
   * The y-coordinate of the tech. Used by
   * {@link com.mrbbot.civilisation.ui.game.UITechTree} to position a tech in
   * the tree.
   */
  private final int y;
  /**
   * The colour of the tech. Used by
   * {@link com.mrbbot.civilisation.ui.game.UITechTree} when displaying a tech.
   */
  private final Color colour;
  /**
   * The techs that this tech requires to be unlocked before it itself can be
   * unlocked.
   */
  private final ArrayList<Tech> requirements;
  /**
   * The techs that require this tech before they can be unlocked. Used by
   * {@link com.mrbbot.civilisation.ui.game.UITechTree} to traverse and render
   * the tech tree.
   */
  private final ArrayList<Tech> requiredBy;
  /**
   * The unlockable items that this tech unlocks. These could be improvements,
   * buildings, or unit types.
   */
  private final ArrayList<Unlockable> unlocks;
  /**
   * The total science cost of unlocking this technology. The default value of
   * -1 indicates that the cost has not yet been calculated. Upon calling
   * {@link Tech#getScienceCost()} for the first time it will be calculated
   * and stored.
   */
  private int scienceCost = -1;

  /**
   * Constructor to create a new tech object. Should only be called from within
   * this class.
   *
   * @param name   user facing name of the tech
   * @param x      x-coordinate of the tech in the tree
   * @param y      y-coordinate of the tech in the tree
   * @param colour colour of the tech in the tree
   */
  private Tech(String name, int x, int y, Color colour) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.colour = colour;

    // Initialise these as empty lists
    this.requirements = new ArrayList<>();
    this.requiredBy = new ArrayList<>();
    this.unlocks = new ArrayList<>();

    // Add this tech to the registry automatically
    REGISTRY.add(this);
  }

  /**
   * Marks this tech as requiring another before it can be unlocked. Allows the
   * caller to write tech.requires(other) meaning a tree can be constructed
   * with an English like language.
   *
   * @param tech the required tech
   */
  private void requires(Tech tech) {
    requirements.add(tech);
    tech.requiredBy.add(this);
  }

  /**
   * Marks this tech as unlocking the specified unlockable. Allows the caller
   * to write tech.unlocks(something).
   *
   * @param unlockable an item this tech unlocks
   */
  private void unlocks(Unlockable unlockable) {
    unlocks.add(unlockable);
  }

  /**
   * Gets the user facing name of this technology
   *
   * @return user facing name to be displayed in the tech tree
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the x-coordinate of this technology
   *
   * @return x-coordinate for positioning this tech in the tech tree
   */
  public int getX() {
    return x;
  }

  /**
   * Gets the y-coordinate of this technology
   *
   * @return y-coordinate for positioning this tech in the tech tree
   */
  public int getY() {
    return y;
  }

  /**
   * Gets the colour of this technology
   *
   * @return colour used for rendering this tech in the tech tree
   */
  public Color getColour() {
    return colour;
  }

  /**
   * Gets a list of the techs required by this tech before it can be unlocked
   *
   * @return list of requirements
   */
  public ArrayList<Tech> getRequirements() {
    return requirements;
  }

  /**
   * Gets a list of the techs that require this tech before they can be
   * unlocked.
   *
   * @return list of techs that require this tech
   */
  public ArrayList<Tech> getRequiredBy() {
    return requiredBy;
  }

  /**
   * Gets a list of the unlockables that this tech unlocks and allows the
   * player to use. These could be improvements, buildings, or unit types.
   *
   * @return list of things this tech unlocks
   */
  public ArrayList<Unlockable> getUnlocks() {
    return unlocks;
  }

  /**
   * Calculates and stores the science cost of unlocking this tech. This
   * function should be called in order of tech level as the cost depends on
   * the techs previous requirements (and their requirements, and so fourth)
   *
   * @return the total science cost of unlocking this tech
   */
  public int getScienceCost() {
    // We only want to calculate this once as it's recursive and could
    // potentially take a long time
    if (scienceCost == -1) {
      if (requirements.size() == 0) {
        // If there aren't any requirements, the cost is 0 (i.e. it's already
        // unlocked)
        scienceCost = 0;
      } else {
        // Otherwise the cost is the sum of the requirements' costs + 25
        scienceCost = requirements.stream()
          .mapToInt(Tech::getScienceCost)
          .sum()
          + 25;
      }
    }
    return scienceCost;
  }

  /**
   * Checks if a player can unlock this tech given their previously unlocked
   * techs. In other words, this function checks all the tech's requirements
   * are fulfilled.
   *
   * @param unlockedTechs all the techs a player has unlocked
   * @return whether or not the player has met all the requirements
   */
  public boolean canUnlockGivenUnlocked(Set<Tech> unlockedTechs) {
    return unlockedTechs.containsAll(requirements) ||
      (requirements.size() == 1
        && requirements.get(0).requirements.size() == 0);
  }

  @Override
  public int hashCode() {
    // Name of the tech should be unique
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Tech) {
      // Name of the tech should be unique
      return name.equals(((Tech) obj).name);
    }
    return false;
  }
}
