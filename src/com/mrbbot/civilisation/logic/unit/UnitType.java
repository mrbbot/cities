package com.mrbbot.civilisation.logic.unit;

import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.net.packet.PacketUnitCreate;
import com.mrbbot.civilisation.ui.game.BadgeType;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static com.mrbbot.civilisation.logic.unit.UnitAbility.*;

/**
 * Class representing a type of unit that can be built within a city. The class
 * contains a variety of constants for the different types of units.
 */
public class UnitType extends CityBuildable {
  /**
   * Base unlock ID for unit types. Used to identify unit types that can be
   * unlocked.
   */
  private static int BASE_UNLOCK_ID = 0x20;

  /*
   * START UNIT TYPE DEFINITIONS
   */
  public static UnitType SETTLER = new UnitType(
    "Settler",
    "Creates cities",
    60,
    0x00,
    Color.GOLD,
    1,
    0,
    5,
    ABILITY_MOVEMENT + ABILITY_SETTLE
  );
  public static UnitType SCOUT = new UnitType(
    "Scout",
    "Moves around",
    40,
    0x00,
    Color.GREEN,
    4,
    5,
    25,
    ABILITY_MOVEMENT + ABILITY_ATTACK
  );
  public static UnitType WARRIOR = new UnitType(
    "Warrior",
    "Can attack adjacent units",
    50,
    0x00,
    Color.RED,
    2,
    10,
    50,
    ABILITY_MOVEMENT + ABILITY_ATTACK
  );
  public static UnitType SWORDSMAN = new UnitType(
    "Swordsman",
    "Can attack adjacent units",
    70,
    BASE_UNLOCK_ID,
    Color.BROWN.darker(),
    2,
    15,
    60,
    ABILITY_MOVEMENT + ABILITY_ATTACK
  );
  public static UnitType KNIGHT = new UnitType(
    "Knight",
    "Can attack adjacent units",
    90,
    BASE_UNLOCK_ID + 1,
    Color.GREY,
    2,
    20,
    70,
    ABILITY_MOVEMENT + ABILITY_ATTACK
  );
  public static UnitType ARCHER = new UnitType(
    "Archer",
    "Can attack units up to 2 tiles away",
    60,
    BASE_UNLOCK_ID + 2,
    Color.INDIANRED,
    2,
    5,
    30,
    ABILITY_MOVEMENT + ABILITY_RANGED_ATTACK
  );
  public static UnitType WORKER = new UnitType(
    "Worker",
    "Can improve a tile",
    40,
    BASE_UNLOCK_ID + 3,
    Color.DODGERBLUE,
    3,
    0,
    15,
    ABILITY_MOVEMENT + ABILITY_IMPROVE
  );
  public static UnitType ROCKET = new UnitType(
    "Rocket",
    "Wins the game",
    200,
    BASE_UNLOCK_ID + 4,
    Color.GREY.darker().darker().darker(),
    0,
    0,
    100,
    ABILITY_BLAST_OFF
  );
  /*
   * END UNIT TYPE DEFINITIONS
   */

  //Define unit upgrade paths
  static {
    WARRIOR.canUpgradeTo = SWORDSMAN;
    SWORDSMAN.canUpgradeTo = KNIGHT;
  }

  /**
   * Array containing all defined unit types.
   */
  public static UnitType[] VALUES = new UnitType[]{
    SETTLER,
    SCOUT,
    WARRIOR,
    SWORDSMAN,
    KNIGHT,
    ARCHER,
    WORKER,
    ROCKET
  };

  /**
   * Function to get a unit type from just its name
   *
   * @param name name of unit type to get
   * @return the unit type with the specified name or null if the unit type
   * doesn't exist
   */
  public static UnitType fromName(String name) {
    // Iterates through all the unit types...
    for (UnitType value : VALUES) {
      // Check if the names match
      if (value.name.equals(name)) return value;
    }
    return null;
  }

  /**
   * Colour representing this unit (the torso colour, the other belt colour
   * represents the player)
   */
  private final Color color;
  /**
   * Base number of movement points units of this type should start with
   */
  private final int movementPoints;
  /**
   * Attack strength of this unit type (i.e. how much damage it will do to
   * other units or cities)
   */
  private final int attackStrength;
  /**
   * Starting health for the unit
   */
  private final int baseHealth;
  /**
   * A number representing this unit's abilities (see {@link UnitAbility}) for
   * more information on how this works.
   */
  private final int abilities;
  /**
   * A unit type that this unit can be upgraded to if it's been unlocked.
   */
  private UnitType canUpgradeTo;

  private UnitType(
    String name,
    String description,
    int productionCost,
    int unlockId,
    Color color,
    int movementPoints,
    int attackStrength,
    int baseHealth,
    int abilities
  ) {
    super(name, description, productionCost, unlockId);
    this.color = color;
    this.movementPoints = movementPoints;
    this.attackStrength = attackStrength;
    this.baseHealth = baseHealth;
    this.abilities = abilities;
  }

  /**
   * Gets the unit's colour
   *
   * @return unit's colour
   */
  public Color getColor() {
    return color;
  }

  /**
   * Gets the unit's base movement points
   *
   * @return unit's base movement points
   */
  @SuppressWarnings("WeakerAccess")
  public int getMovementPoints() {
    return movementPoints;
  }

  /**
   * Gets the unit's attack strength
   *
   * @return unit's attack strength
   */
  public int getAttackStrength() {
    return attackStrength;
  }

  /**
   * Gets the unit's base health
   *
   * @return unit's base/starting health
   */
  public int getBaseHealth() {
    return baseHealth;
  }

  /**
   * Gets a number representing the units abilities
   *
   * @return unit's abilities
   */
  int getAbilities() {
    return abilities;
  }

  /**
   * Gets the unit type that this unit type can update to
   *
   * @return upgraded unit type, or null if this unit type cannot be upgraded
   */
  public UnitType getUpgrade() {
    return canUpgradeTo;
  }

  /**
   * Gets the details to be displayed in the city production list for this
   * unit type
   *
   * @return details to be displayed
   */
  @Override
  public ArrayList<Detail> getDetails() {
    ArrayList<Detail> details = super.getDetails();

    details.add(new Detail(BadgeType.HEALTH, baseHealth));
    if (movementPoints != 0)
      details.add(new Detail(BadgeType.MOVEMENT, movementPoints));
    if (attackStrength != 0)
      details.add(new Detail(BadgeType.ATTACK, attackStrength));

    return details;
  }

  /**
   * Builds an instance of this unit type in the specified city, creating a new
   * unit object
   *
   * @param city city to build in
   * @param game game the city is contained within
   * @return tile to update the render of (i.e. the tile the new unit was
   * created in)
   */
  @Override
  public Tile build(City city, Game game) {
    PacketUnitCreate packetUnitCreate = new PacketUnitCreate(
      city.player.id,
      city.getX(), city.getY(),
      this
    );
    Tile[] placedTiles = game.handlePacket(packetUnitCreate);
    return placedTiles != null
      && placedTiles.length > 0
      ? placedTiles[0]
      : null;
    // No need to broadcast packet as this method is called on the client and
    // the server automatically on receiving a PacketReady
  }
}
