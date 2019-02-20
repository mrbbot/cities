package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.unit.UnitType;

public class PacketUnitCreate extends PacketUpdate {
  public final String id;
  public final int x, y;
  private final String unitType;

  public PacketUnitCreate(String id, int x, int y, UnitType unitType) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.unitType = unitType.getName();
  }

  public UnitType getUnitType() {
    return UnitType.fromName(unitType);
  }

  @Override
  public String getName() {
    return "unit-create";
  }
}
