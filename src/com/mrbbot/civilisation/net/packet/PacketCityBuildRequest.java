package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.CityBuildable;

/**
 * Packet emitted when a player clicks on a buildable in the city production
 * list. On receiving this packet, the game should start the build of this in
 * the selected city, or purchase the item with gold if that was requested.
 */
public class PacketCityBuildRequest extends PacketUpdate {
  /**
   * X-coordinate of the city to build in
   */
  public final int x;
  /**
   * Y-coordinate of the city to build in
   */
  public final int y;
  /**
   * Name of the buildable object to build in the city (buildables aren't
   * serializable so we must store the unique name instead)
   */
  private final String buildable;
  /**
   * Whether to build this with production or to just purchase it outright
   * with gold.
   */
  public final boolean withProduction;

  public PacketCityBuildRequest(
    int x,
    int y,
    CityBuildable buildable,
    boolean withProduction
  ) {
    this.x = x;
    this.y = y;
    this.buildable = buildable.getName();
    this.withProduction = withProduction;
  }

  /**
   * Gets the city buildable this packet contains. Buildables aren't
   * serializable so they must be recreated from their name.
   *
   * @return buildable this packet contains
   */
  public CityBuildable getBuildable() {
    return CityBuildable.fromName(buildable);
  }
}
