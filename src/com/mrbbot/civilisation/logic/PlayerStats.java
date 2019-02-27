package com.mrbbot.civilisation.logic;

/**
 * Data class containing information about a player's statistics (their
 * science and gold) so that they can be displayed in the UI
 */
public class PlayerStats {
  /**
   * The player's total science per turn from all their cities
   */
  public final int sciencePerTurn;
  /**
   * The player's total gold amount
   */
  public final int gold;
  /**
   * The player's total gold per turn from all their cities
   */
  public final int goldPerTurn;

  public PlayerStats(int sciencePerTurn, int gold, int goldPerTurn) {
    this.sciencePerTurn = sciencePerTurn;
    this.gold = gold;
    this.goldPerTurn = goldPerTurn;
  }
}
