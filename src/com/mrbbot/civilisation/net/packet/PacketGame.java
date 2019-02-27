package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.map.Game;

import java.util.Map;

/**
 * Packet emitted a new user joins the game containing the current game state.
 * On receiving this packet, the game should load the state and initialise and
 * display the 3D game render.
 */
public class PacketGame extends Packet {
  /**
   * Map containing the game state. See {@link Game#toMap()}.
   */
  public final Map<String, Object> map;

  public PacketGame(Map<String, Object> map) {
    this.map = map;
  }
}
