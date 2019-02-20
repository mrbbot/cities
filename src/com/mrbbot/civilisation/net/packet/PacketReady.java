package com.mrbbot.civilisation.net.packet;

public class PacketReady extends Packet {
  public final boolean ready;

  public PacketReady(boolean ready) {
    this.ready = ready;
  }
}
