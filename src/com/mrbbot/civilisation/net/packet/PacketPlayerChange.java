package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted by the server when a new player joins the game. On receiving
 * this packet, the game should add this player to the list of players.
 */
public class PacketPlayerChange extends Packet {
  public final String id;

  public PacketPlayerChange(String id) {
    this.id = id;
  }
}
