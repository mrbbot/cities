package com.mrbbot.civilisation.net.packet;

public class PacketCityCreate extends Packet {
  public final String id;
  public final int x, y;

  public PacketCityCreate( String id, int x, int y) {
    this.id = id;
    this.x = x;
    this.y = y;
  }

  @Override
  public String getName() {
    return "city-create";
  }
}
