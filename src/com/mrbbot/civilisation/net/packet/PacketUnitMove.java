package com.mrbbot.civilisation.net.packet;

public class PacketUnitMove extends Packet {
  public final int startX, startY, endX, endY;

  public PacketUnitMove(int startX, int startY, int endX, int endY) {
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    this.endY = endY;
  }

  @Override
  public String getName() {
    return "unit-move";
  }
}
