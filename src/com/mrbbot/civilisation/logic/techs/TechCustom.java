package com.mrbbot.civilisation.logic.techs;

import com.mrbbot.civilisation.logic.interfaces.Unlockable;
import javafx.scene.paint.Color;

public class TechCustom extends Tech {
  private final String title;
  private final int x;
  private final int y;
  private final Color color;
  private final Unlockable[] unlocks;

  public TechCustom(String title, int x, int y, Color color, String... unlocks) {
    this.title = title;
    this.x = x;
    this.y = y;
    this.color = color;
    this.unlocks = new Unlockable[unlocks.length];
    for (int i = 0; i < unlocks.length; i++) {
      final String unlock = unlocks[i];
      this.unlocks[i] = () -> unlock;
    }
  }

  @Override
  public String getName() {
    return title;
  }

  @Override
  public int getX() {
    return x;
  }

  @Override
  public int getY() {
    return y;
  }

  @Override
  public Unlockable[] getUnlocks() {
    return unlocks;
  }

  @Override
  public Color getColour() {
    return color;
  }

  @Override
  public int hashCode() {
    return title.hashCode();
  }
}
