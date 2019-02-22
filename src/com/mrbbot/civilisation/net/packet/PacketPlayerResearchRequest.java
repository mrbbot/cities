package com.mrbbot.civilisation.net.packet;

import com.mrbbot.civilisation.logic.techs.Tech;

public class PacketPlayerResearchRequest extends PacketUpdate {
  public final String playerId;
  private final String techName;

  public PacketPlayerResearchRequest(String playerId, Tech tech) {
    this.playerId = playerId;
    this.techName = tech.getName();
  }

  public Tech getTech() {
    return Tech.fromName(techName);
  }
}
