package com.mrbbot.civilisation.net.packet;

public class PacketBlastOff extends PacketUpdate {
  public String playerId;

  public PacketBlastOff(String playerId) {
    this.playerId = playerId;
  }
}
