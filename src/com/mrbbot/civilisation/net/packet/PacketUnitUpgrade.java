package com.mrbbot.civilisation.net.packet;

public class PacketUnitUpgrade extends PacketUpdate {
  public final int x, y;

  public PacketUnitUpgrade(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
