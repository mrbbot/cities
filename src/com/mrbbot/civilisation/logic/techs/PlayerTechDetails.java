package com.mrbbot.civilisation.logic.techs;

import java.util.Set;

/**
 * Class containing details about a player's tech status. Includes their
 * unlocked techs, the tech they're currently unlocking, and the percent
 * unlocked of the current tech.
 */
public class PlayerTechDetails {
  /**
   * Techs that have been unlocked by the player. This is a Set because each
   * player can only unlock each tech once, so there should be no duplicate
   * entries.
   */
  public Set<Tech> unlockedTechs;
  /**
   * Tech that the player is currently unlocking. May be null if the player is
   * not researching anything at the moment.
   */
  public Tech currentlyUnlocking;
  /**
   * The progress of the current research project. This is a value in the range
   * 0 <= percentUnlocked <= 1. If the player isn't researching anything at the
   * moment, this value will be 0.
   */
  public double percentUnlocked;

  public PlayerTechDetails(
    Set<Tech> unlockedTechs,
    Tech currentlyUnlocking,
    double percentUnlocked
  ) {
    this.unlockedTechs = unlockedTechs;
    this.currentlyUnlocking = currentlyUnlocking;
    this.percentUnlocked = percentUnlocked;
  }
}
