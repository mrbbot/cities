package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a unit attacks another living thing (city or other unit).
 * On receiving this packet, the game should check if the attack is valid, and
 * then perform the attack.
 */
public class PacketDamage extends PacketUpdate {
  /**
   * X-coordinate of the attacking unit
   */
  public final int attackerX;
  /**
   * Y-coordinate of the attacking unit
   */
  public final int attackerY;
  /**
   * X-coordinate of the target living thing (city or another unit)
   */
  public final int targetX;
  /**
   * Y-coordinate of the target living thing (city or another unit)
   */
  public final int targetY;

  public PacketDamage(int attackerX, int attackerY, int targetX, int targetY) {
    this.attackerX = attackerX;
    this.attackerY = attackerY;
    this.targetX = targetX;
    this.targetY = targetY;
  }
}
