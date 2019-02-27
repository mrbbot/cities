package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a player activates the blast off action. On receiving
 * this packet, the specified player should win the game.
 */
public class PacketBlastOff extends PacketUpdate {
  /**
   * ID of the player who blasted off and is now the winner of the game
   */
  public String playerId;

  public PacketBlastOff(String playerId) {
    this.playerId = playerId;
  }
}
