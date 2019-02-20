package com.mrbbot.civilisation.net.packet;

public class PacketCityRename extends PacketUpdate {
  public final int x, y;
  public final String newName;

  public PacketCityRename(int x, int y, String newName) {
    this.x = x;
    this.y = y;
    this.newName = newName;
  }

  @Override
  public String getName() {
    return "city-rename";
  }
}
