package com.mrbbot.civilisation.net.packet;

public class PacketChat extends PacketUpdate {
  public final String message;

  public PacketChat(String message) {
    this.message = message;
  }
}
