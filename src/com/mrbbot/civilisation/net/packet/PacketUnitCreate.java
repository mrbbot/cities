package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.unit.UnitType;

public class PacketUnitCreate {
  public final String id;
  public final int x, y;
  public final UnitType unitType;

  public PacketUnitCreate(String id, int x, int y, UnitType unitType) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.unitType = unitType;
  }
}
