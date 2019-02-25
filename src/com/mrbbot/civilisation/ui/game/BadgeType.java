package com.mrbbot.civilisation.ui.game;

import javafx.scene.paint.Color;

public enum BadgeType {
  SCIENCE(Color.DEEPSKYBLUE, "S"),
  GOLD(Color.GOLD, "£"),
  PRODUCTION(Color.ORANGE, "P"),
  FOOD(Color.GREEN, "@"),
  HEALTH(Color.PINK, "H"),
  MOVEMENT(Color.LIMEGREEN, "M"),
  ATTACK(Color.CRIMSON, "!");

  Color color;
  Color textColor;
  String text;

  BadgeType(Color color, String text) {
    this.color = color;
    this.textColor = color.darker();
    this.text = text;
  }
}
