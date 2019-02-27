package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a player wants to create a city. On receiving this
 * packet, the game should create the city at the specified coordinates for the
 * player.
 */
public class PacketCityCreate extends PacketUpdate {
  /**
   * ID of the player who's creating this city
   */
  public final String id;
  /**
   * X-coordinate of the city
   */
  public final int x;
  /**
   * Y-coordinate of the city
   */
  public final int y;

  public PacketCityCreate(String id, int x, int y) {
    this.id = id;
    this.x = x;
    this.y = y;
  }
}
