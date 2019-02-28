package com.mrbbot.civilisation.ui.game;

import javafx.scene.paint.Color;

/**
 * Enum representing the different types of badges. Badges are just a coloured
 * circle with some text on top.
 */
public enum BadgeType {
  SCIENCE(Color.DEEPSKYBLUE, "S"),
  GOLD(Color.GOLD, "£"),
  PRODUCTION(Color.ORANGE, "P"),
  FOOD(Color.GREEN, "@"),
  HEALTH(Color.PINK, "H"),
  MOVEMENT(Color.LIMEGREEN, "M"),
  ATTACK(Color.CRIMSON, "!");

  /**
   * Colour of the circle
   */
  Color color;
  /**
   * Colour of the text on the circle
   */
  Color textColor;
  /**
   * Text to be displayed on the circle
   */
  String text;

  BadgeType(Color color, String text) {
    this.color = color;
    this.textColor = color.darker();
    this.text = text;
  }
}
