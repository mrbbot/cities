package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.map.Map;

public class PacketMap extends Packet {
  public final Map map;

  public PacketMap(Map map) {
    this.map = map;
  }

  @Override
  public String getName() {
    return "map";
  }
}
