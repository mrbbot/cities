package com.mrbbot.civilisation.net.packet;

public class PacketUnitMove extends PacketUpdate {
  public final int startX, startY, endX, endY, usedMovementPoints;

  public PacketUnitMove(int startX, int startY, int endX, int endY, int usedMovementPoints) {
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    this.endY = endY;
    this.usedMovementPoints = usedMovementPoints;
  }
}
