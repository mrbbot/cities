package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.map.tile.Improvement;

/**
 * Packet emitted when a player requests that a worker improve a tile. On
 * receiving this packet, the game should set the workers current build project
 * to the specified one.
 */
public class PacketWorkerImproveRequest extends PacketUpdate {
  /**
   * X-coordinate of the tile containing the worker where the improvement
   * should built
   */
  public final int x;
  /**
   * Y-coordinate of the tile containing the worker where the improvement
   * should built
   */
  public final int y;
  /**
   * Name of the improvement to be built (improvements aren't serializable so
   * only the name is stored)
   */
  private String improvementName;

  public PacketWorkerImproveRequest(int x, int y, Improvement improvement) {
    this.x = x;
    this.y = y;
    this.improvementName = improvement.name;
  }

  /**
   * Finds the improvement from the name contained in the packet
   *
   * @return improvement specified by this packet
   */
  public Improvement getImprovement() {
    return Improvement.fromName(improvementName);
  }
}
