package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.CityBuildable;

public class PacketCityBuildRequest extends PacketUpdate {
  public final int x, y;
  private final String buildable;

  public PacketCityBuildRequest(int x, int y, CityBuildable buildable) {
    this.x = x;
    this.y = y;
    this.buildable = buildable.getName();
  }

  public CityBuildable getBuildable() {
    return CityBuildable.fromName(buildable);
  }
}
