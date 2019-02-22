package com.mrbbot.civilisation.logic.techs;

import java.util.Set;

public class PlayerTechDetails {
  public Set<Tech> unlockedTechs;
  public Tech currentlyUnlocking;
  public double percentUnlocked;

  public PlayerTechDetails(Set<Tech> unlockedTechs, Tech currentlyUnlocking, double percentUnlocked) {
    this.unlockedTechs = unlockedTechs;
    this.currentlyUnlocking = currentlyUnlocking;
    this.percentUnlocked = percentUnlocked;
  }
}
