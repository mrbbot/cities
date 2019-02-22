package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.map.tile.Improvement;

public class PacketWorkerImproveRequest extends PacketUpdate {
  public final int x, y;
  private String improvementName;

  public PacketWorkerImproveRequest(int x, int y, Improvement improvement) {
    this.x = x;
    this.y = y;
    this.improvementName = improvement.name;
  }

  public Improvement getImprovement() {
    return Improvement.fromName(improvementName);
  }
}
