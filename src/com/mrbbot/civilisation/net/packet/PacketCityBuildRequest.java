package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.CityBuildable;

public class PacketCityBuildRequest extends PacketUpdate {
  public final int x, y;
  private final String buildable;
  public final boolean withProduction;

  public PacketCityBuildRequest(int x, int y, CityBuildable buildable, boolean withProduction) {
    this.x = x;
    this.y = y;
    this.buildable = buildable.getName();
    this.withProduction = withProduction;
  }

  public CityBuildable getBuildable() {
    return CityBuildable.fromName(buildable);
  }
}
