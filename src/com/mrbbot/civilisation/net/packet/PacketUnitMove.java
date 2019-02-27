package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a selected unit is moved across the map. On receiving
 * this packet, the unit should be moved and the used movement points should be
 * deducted from the units remaining count.
 */
public class PacketUnitMove extends PacketUpdate {
  /**
   * X-coordinate of the unit's current tile
   */
  public final int startX;
  /**
   * Y-coordinate of the unit's current tile
   */
  public final int startY;
  /**
   * X-coordinate of the target tile
   */
  public final int endX;
  /**
   * Y-coordinate of the target tile
   */
  public final int endY;
  /**
   * How many movement points should be consumed by moving. Depends on the path
   * taken.
   */
  public final int usedMovementPoints;

  public PacketUnitMove(
    int startX,
    int startY,
    int endX,
    int endY,
    int usedMovementPoints
  ) {
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    this.endY = endY;
    this.usedMovementPoints = usedMovementPoints;
  }
}
