package com.mrbbot.civilisation.net.packet;

import java.util.Map;

public class PacketGame extends Packet {
  public final Map<String, Object> map;

  public PacketGame(Map<String, Object> map) {
    this.map = map;
  }

  @Override
  public String getName() {
    return "game";
  }
}
