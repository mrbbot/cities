package com.mrbbot.civilisation.logic;

import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.Random;

public class Player implements Serializable {
  public String id;

  public Player(String id) {
    this.id = id;
  }

  private double makeColourPart(Random random) {
    return (random.nextDouble() * 0.5) + 0.5;
  }

  public Color getColour() {
    Random random = new Random(id.hashCode());
    return new Color(
      makeColourPart(random),
      makeColourPart(random),
      makeColourPart(random),
      1.0
    );
  }
}
