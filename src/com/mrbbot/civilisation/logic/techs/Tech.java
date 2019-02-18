package com.mrbbot.civilisation.logic.techs;

import com.mrbbot.civilisation.logic.interfaces.Positionable;
import com.mrbbot.civilisation.logic.interfaces.Unlockable;
import javafx.scene.paint.Color;

public abstract class Tech implements Unlockable, Positionable {
  public abstract Unlockable[] getUnlocks();
  public abstract Color getColour();
}
