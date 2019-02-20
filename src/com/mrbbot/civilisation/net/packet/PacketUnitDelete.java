package com.mrbbot.civilisation.net.packet;

public class PacketUnitDelete extends PacketUpdate {
  public final int x, y;

  public PacketUnitDelete(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
