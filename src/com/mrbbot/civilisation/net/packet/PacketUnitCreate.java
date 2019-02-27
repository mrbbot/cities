package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.unit.UnitType;

/**
 * Packet emitted when a player creates a unit (by building one in a city, or
 * by starting the game with some). On receiving this packet, the game should
 * create a unit belonging to the specified player with the specified type and
 * try to place it as close as possible to the target location.
 */
public class PacketUnitCreate extends PacketUpdate {
  /**
   * ID of the player to create the unit for
   */
  public final String id;
  /**
   * X-coordinate of the tile to place the unit close to
   */
  public final int x;
  /**
   * Y-coordinate of the tile to place the unit close to
   */
  public final int y;
  /**
   * Type of unit to create (unit type's aren't serializable so only the name
   * is stored)
   */
  private final String unitType;

  public PacketUnitCreate(String id, int x, int y, UnitType unitType) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.unitType = unitType.getName();
  }

  /**
   * Finds the unit type specified by the name is this packet
   *
   * @return the unit type specified by this packet
   */
  public UnitType getUnitType() {
    return UnitType.fromName(unitType);
  }
}
