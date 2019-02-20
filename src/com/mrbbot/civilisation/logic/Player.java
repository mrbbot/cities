package com.mrbbot.civilisation.logic;

import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.Random;

public class Player implements Serializable {
  public String id;

  public Player(String id) {
    this.id = id;
  }

  public Color getColour() {
    return Color.hsb(id.hashCode() % 360, 1, 1);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Player) {
      return id.equals(((Player) obj).id);
    }
    return false;
  }
}
