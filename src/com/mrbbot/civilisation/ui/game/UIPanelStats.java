package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.PlayerStats;
import com.mrbbot.generic.net.ClientOnly;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * UI panel for displaying the current player's statistics (gold total, gold
 * per turn, science per turn)
 */
@ClientOnly
public class UIPanelStats extends HBox {
  /**
   * Label for displaying player's science per turn
   */
  private Label scienceLabel;
  /**
   * Label for displaying player's current gold total and their gold per turn
   */
  private Label goldLabel;

  UIPanelStats() {
    // Initialise horizontal box with 10px of horizontal spacing between
    // components
    super(10);

    // Initialise and add the labels and badges to the panel
    scienceLabel = new Label("");
    goldLabel = new Label("");

    getChildren().addAll(
      new Badge(BadgeType.SCIENCE),
      scienceLabel,
      new Badge(BadgeType.GOLD),
      goldLabel
    );
  }

  /**
   * Update the labels' text to reflect the player's new stats
   * @param playerStats object containing the new statistics for the player
   */
  void setPlayerStats(PlayerStats playerStats) {
    scienceLabel.setText(String.valueOf(playerStats.sciencePerTurn));
    goldLabel.setText(String.format(
      "%d (+%d)",
      playerStats.gold,
      playerStats.goldPerTurn
    ));
  }
}
