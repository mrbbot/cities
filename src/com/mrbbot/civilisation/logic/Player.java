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
    Random random = new Random(id.hashCode());
    return Color.hsb(random.nextInt(360), 1, 1);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Player) {
      return id.equals(((Player) obj).id);
    }
    return false;
  }
}
