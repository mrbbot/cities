package com.mrbbot.civilisation.logic.techs;

public abstract class Tech implements Unlockable, Positionable, Colourable {
  public abstract Unlockable[] getUnlocks();
}
