package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a user requests that a unit is upgraded to an improved
 * type. On receiving this packet, the game should upgrade the unit type, and
 * proportionally set the health of the unit (see
 * {@link com.mrbbot.civilisation.logic.Living#setBaseHealth(int)}).
 */
public class PacketUnitUpgrade extends PacketUpdate {
  /**
   * X-coordinate of tile containing unit to upgrade
   */
  public final int x;
  /**
   * Y-coordinate of tile containing unit to upgrade
   */
  public final int y;

  public PacketUnitUpgrade(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
