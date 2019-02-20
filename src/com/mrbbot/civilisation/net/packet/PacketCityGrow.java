package com.mrbbot.civilisation.net.packet;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class PacketCityGrow extends PacketUpdate {
  public final String id;
  public final int x, y;
  private final int[] grownToXs;
  private final int[] grownToYs;

  public PacketCityGrow(String id, int x, int y, ArrayList<Point2D> grownTo) {
    this.id = id;
    this.x = x;
    this.y = y;

    int grownToSize = grownTo.size();
    grownToXs = new int[grownToSize];
    grownToYs = new int[grownToSize];
    for (int i = 0; i < grownToSize; i++) {
      grownToXs[i] = (int) grownTo.get(i).getX();
      grownToYs[i] = (int) grownTo.get(i).getY();
    }
  }

  public ArrayList<Point2D> getGrownTo() {
    int grownToSize = grownToXs.length;
    ArrayList<Point2D> grownTo = new ArrayList<>(grownToSize);
    for (int i = 0; i < grownToSize; i++) {
      grownTo.add(new Point2D(grownToXs[i], grownToYs[i]));
    }
    return grownTo;
  }
}
