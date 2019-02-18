package com.mrbbot.civilisation.net.packet;

public class PacketPlayerChange extends Packet {
  public final String id;

  public PacketPlayerChange(String id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return "player-change";
  }
}
