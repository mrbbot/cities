package com.mrbbot.civilisation.net.packet;

public class PacketPlayerChange extends Packet {
  public final String id;
  public final boolean exists;

  public PacketPlayerChange(String id, boolean exists) {
    this.id = id;
    this.exists = exists;
  }

  @Override
  public String getName() {
    return "player-change";
  }
}
