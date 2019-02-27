package com.mrbbot.civilisation.net.packet;

import javafx.geometry.Point2D;

import java.util.ArrayList;

/**
 * Packet emitted when a city grows to a new set of tiles. On receiving this
 * packet, the game should add the specified tiles to the cities territory.
 */
public class PacketCityGrow extends PacketUpdate {
  /**
   * ID of the owner of the city to grow
   */
  public final String id;
  /**
   * X-coordinate of the city to grow
   */
  public final int x;
  /**
   * Y-coordinate of the city to grow
   */
  public final int y;
  /**
   * X-coordinates of the new set of tiles to grow to ({@link Point2D} isn't
   * serializable)
   */
  private final int[] grownToXs;
  /**
   * Y-coordinates of the new set of tiles to grow to ({@link Point2D} isn't
   * serializable)
   */
  private final int[] grownToYs;

  public PacketCityGrow(String id, int x, int y, ArrayList<Point2D> grownTo) {
    this.id = id;
    this.x = x;
    this.y = y;

    // Split the grown to coordinates into their x and y components so they
    // can be serialized
    int grownToSize = grownTo.size();
    grownToXs = new int[grownToSize];
    grownToYs = new int[grownToSize];
    for (int i = 0; i < grownToSize; i++) {
      grownToXs[i] = (int) grownTo.get(i).getX();
      grownToYs[i] = (int) grownTo.get(i).getY();
    }
  }

  /**
   * Reconstructs the grown to coordinates from their x and y components
   * @return coordinates of tiles the city should grow to
   */
  public ArrayList<Point2D> getGrownTo() {
    int grownToSize = grownToXs.length;
    ArrayList<Point2D> grownTo = new ArrayList<>(grownToSize);
    for (int i = 0; i < grownToSize; i++) {
      grownTo.add(new Point2D(grownToXs[i], grownToYs[i]));
    }
    return grownTo;
  }
}
