package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a user requests the specified city should have a
 * different name. On receiving this packet, the name of the city should be
 * updated.
 */
public class PacketCityRename extends PacketUpdate {
  /**
   * X-coordinate of the city to rename
   */
  public final int x;
  /**
   * Y-coordinate of the city to rename
   */
  public final int y;
  /**
   * New name the user has chosen for the city
   */
  public final String newName;

  public PacketCityRename(int x, int y, String newName) {
    this.x = x;
    this.y = y;
    this.newName = newName;
  }
}
