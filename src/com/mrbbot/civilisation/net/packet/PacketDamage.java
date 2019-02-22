package com.mrbbot.civilisation.net.packet;

public class PacketDamage extends PacketUpdate {
  public final int attackerX, attackerY, targetX, targetY;

  public PacketDamage(int attackerX, int attackerY, int targetX, int targetY) {
    this.attackerX = attackerX;
    this.attackerY = attackerY;
    this.targetX = targetX;
    this.targetY = targetY;
  }
}
