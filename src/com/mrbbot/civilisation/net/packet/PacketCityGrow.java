package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.net.serializable.SerializableIntPoint2D;

import java.util.ArrayList;

public class PacketCityGrow extends PacketUpdate {
  public final String id;
  public final int x, y;
  public final ArrayList<SerializableIntPoint2D> grownTo;

  public PacketCityGrow(String id, int x, int y, ArrayList<SerializableIntPoint2D> grownTo) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.grownTo = grownTo;
  }

  @Override
  public String getName() {
    return "city-grow";
  }
}
