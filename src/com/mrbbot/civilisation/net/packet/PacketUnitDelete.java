package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a unit should be removed from the game for some reason
 * (settler settling, unit death, etc). On receiving this packet, the unit at
 * the specified coordinate should be removed from the game.
 */
public class PacketUnitDelete extends PacketUpdate {
  /**
   * X-coordinate of the tile containing the unit to be removed
   */
  public final int x;
  /**
   * Y-coordinate of the tile containing the unit to be removed
   */
  public final int y;

  public PacketUnitDelete(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
