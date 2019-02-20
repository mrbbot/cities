package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.PlayerStats;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class UIPanelStats extends HBox {
  private Label scienceLabel, goldLabel;

  UIPanelStats() {
    super(10);

    scienceLabel = new Label("");
    goldLabel = new Label("");

    getChildren().addAll(
      new Badge(BadgeType.SCIENCE),
      scienceLabel,
      new Badge(BadgeType.GOLD),
      goldLabel
    );
  }

  void setPlayerStats(PlayerStats playerStats) {
    scienceLabel.setText(String.valueOf(playerStats.sciencePerTurn));
    goldLabel.setText(String.format("%d (+%d)", playerStats.gold, playerStats.goldPerTurn));
  }
}
