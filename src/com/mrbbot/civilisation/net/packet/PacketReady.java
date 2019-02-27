package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.map.Game;

/**
 * Packet emitted by a client when a player marks themselves as ready or by the
 * server when all players have marked themselves as ready. On receiving this
 * packet on the server, the server should mark the player as ready and check
 * if all other players have done the same thing, moving the game onto the next
 * turn. On receiving this packet on the client, the turn should be handled.
 * See {@link Game#handleTurn(Game)}.
 */
public class PacketReady extends Packet {
  /**
   * Whether the player is ready. Should always be true when sending from the
   * client, and false when sending from the server.
   */
  public final boolean ready;

  public PacketReady(boolean ready) {
    this.ready = ready;
  }
}
