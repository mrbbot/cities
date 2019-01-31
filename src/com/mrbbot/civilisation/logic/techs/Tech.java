package com.mrbbot.civilisation.logic.techs;

import com.mrbbot.civilisation.logic.interfaces.Colourable;
import com.mrbbot.civilisation.logic.interfaces.Positionable;
import com.mrbbot.civilisation.logic.interfaces.Unlockable;

public abstract class Tech implements Unlockable, Positionable, Colourable {
  public abstract Unlockable[] getUnlocks();
}
