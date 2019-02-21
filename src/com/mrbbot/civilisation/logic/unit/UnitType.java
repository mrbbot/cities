package com.mrbbot.civilisation.logic.unit;

import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.net.packet.PacketUnitCreate;
import com.mrbbot.civilisation.ui.game.BadgeType;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static com.mrbbot.civilisation.logic.unit.UnitAbility.*;

public class UnitType extends CityBuildable {
  public static int BASE_UNLOCK_ID = 0x20;

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
  public static UnitType ARCHER = new UnitType(
    "Archer",
    "Can attack units up to 2 tiles away",
    60,
    BASE_UNLOCK_ID,
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
    0x00,
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
    BASE_UNLOCK_ID + 1,
    Color.GREY,
    0,
    0,
    100,
    ABILITY_BLAST_OFF
  );

  public static UnitType[] VALUES = new UnitType[]{
    SETTLER,
    SCOUT,
    WARRIOR,
    ARCHER,
    WORKER,
    ROCKET
  };

  public static UnitType fromName(String name) {
    for (UnitType value : VALUES) {
      if(value.name.equals(name)) return value;
    }
    return null;
  }

  private final Color color;
  private final int movementPoints;
  private final int attackStrength;
  private final int baseHealth;
  private final int abilities;

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

  public Color getColor() {
    return color;
  }

  public int getMovementPoints() {
    return movementPoints;
  }

  public int getAttackStrength() {
    return attackStrength;
  }

  public int getBaseHealth() {
    return baseHealth;
  }

  int getAbilities() {
    return abilities;
  }

  @Override
  public ArrayList<Detail> getDetails() {
    ArrayList<Detail> details = super.getDetails();

    details.add(new Detail(BadgeType.HEALTH, baseHealth));
    if(movementPoints != 0) details.add(new Detail(BadgeType.MOVEMENT, movementPoints));
    if(attackStrength != 0) details.add(new Detail(BadgeType.ATTACK, attackStrength));

    return details;
  }

  @Override
  public void build(City city, Game game) {
    PacketUnitCreate packetUnitCreate = new PacketUnitCreate(city.player.id, city.getX(), city.getY(), this);
    game.handlePacket(packetUnitCreate);
    // no need to broadcast packet as this method is called on the client and the server automatically
  }
}
