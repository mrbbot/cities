package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a player sends a chat message from the UI. On receiving
 * this packet, the UI of this game should update to include the new message.
 */
public class PacketChat extends PacketUpdate {
  /**
   * Chat message sent by the player along with their player ID. An example
   * would be "Player: Hello?".
   */
  public final String message;

  public PacketChat(String message) {
    this.message = message;
  }
}
