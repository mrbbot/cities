package com.mrbbot.civilisation.logic;

import javafx.scene.paint.Color;

import java.io.Serializable;

/**
 * Player object containing the player's ID and a function for calculation
 * their colour. Also implements serializable so it can be sent over the
 * network.
 */
public class Player implements Serializable {
  /**
   * ID of this player. Chosen by the user when they launch the game.
   */
  public String id;

  public Player(String id) {
    this.id = id;
  }

  /**
   * Gets the colour of this player. This is calculated from the hash code of
   * the player's id, so the same ID will always have the same colour. There's
   * also no need to send the colour over the network as it can be easily
   * recalculated.
   *
   * @return the colour representing this player to be used for unit rendering
   * and UI panels
   */
  public Color getColour() {
    // Calculate the hue from the hash code, hues can be a number from 0 to
    // 360.
    return Color.hsb(id.hashCode() % 360, 1, 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Player) {
      // The id of the player should be unique
      return id.equals(((Player) obj).id);
    }
    return false;
  }
}
