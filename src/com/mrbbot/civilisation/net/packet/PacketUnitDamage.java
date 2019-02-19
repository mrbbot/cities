package com.mrbbot.civilisation.net.packet;

public class PacketUnitDamage extends PacketUpdate {
  public final int attackerX, attackerY, targetX, targetY;

  public PacketUnitDamage(int attackerX, int attackerY, int targetX, int targetY) {
    this.attackerX = attackerX;
    this.attackerY = attackerY;
    this.targetX = targetX;
    this.targetY = targetY;
  }

  @Override
  public String getName() {
    return "unit-damage";
  }
}
