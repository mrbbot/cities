package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.techs.Tech;

/**
 * Packet emitted when a player starts to research a new tech. On receiving
 * this packet, the game should change the currently researching tech for the
 * player.
 */
public class PacketPlayerResearchRequest extends PacketUpdate {
  /**
   * ID of the player requesting the research change
   */
  public final String playerId;
  /**
   * Name of the tech to be researched (techs aren't serializable themselves so
   * they must be remade on receiving the packet)
   */
  private final String techName;

  public PacketPlayerResearchRequest(String playerId, Tech tech) {
    this.playerId = playerId;
    this.techName = tech.getName();
  }

  /**
   * Gets the unserializable tech from the tech name
   *
   * @return tech specified by this packet
   */
  public Tech getTech() {
    return Tech.fromName(techName);
  }
}
