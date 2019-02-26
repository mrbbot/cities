package com.mrbbot.civilisation.logic.techs;

/**
 * Interface describing an unlockable item. In the context of this game, this
 * would be an improvement, building, or unit type.
 */
public interface Unlockable {
  /**
   * Gets the user facing name for this unlock. Displayed in the tech tree by
   * {@link com.mrbbot.civilisation.ui.game.UITechTree}.
   *
   * @return user facing name for this unlock
   */
  String getName();

  /**
   * Gets a ID representing this unlock. This should be 0 (if the item is
   * unlocked by default) or a unique ID for the item
   *
   * @return ID representing this unlock
   */
  int getUnlockId();
}
