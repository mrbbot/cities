package com.mrbbot.civilisation.logic.unit;

/**
 * Class containing constants for unit abilities. Each of these is an integer
 * with one of the bits set to one. This allows for easy checking of a unit's
 * abilities.
 *
 * For example, if we take the worker's ability number (ABILITY_MOVEMENT +
 * ABILITY_IMPROVE) this will result in the binary number: 10001. If we AND
 * this with the ABILITY_IMPROVE constant and then check if the number is
 * greater than 0, we'll be able to tell if the unit has the improve ability.
 *
 *     10001
 * AND 10000
 *   = 10000 (16) [16 > 0, so the unit has the ability]
 *
 * If we perform the same check with settling:
 *
 *     10001
 * AND 00010
 *   = 00000 (0) [0 is not > 0, so the unit doesn't have the ability]
 */
public final class UnitAbility {
  public static final int ABILITY_MOVEMENT = 0b1;
  public static final int ABILITY_SETTLE = 0b10;
  public static final int ABILITY_ATTACK = 0b100;
  public static final int ABILITY_RANGED_ATTACK = 0b1000;
  public static final int ABILITY_IMPROVE = 0b10000;
  public static final int ABILITY_BLAST_OFF = 0b100000;
}
