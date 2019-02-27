package com.mrbbot.civilisation.logic;

import com.mrbbot.civilisation.geometry.Positionable;
import com.mrbbot.civilisation.logic.unit.Unit;
import javafx.geometry.Point2D;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base living class. Describes something that has health and does
 * something every turn. Implements positionable, mappable, and turn handler
 * interfaces, but doesn't provide any implementations meaning this must be
 * done by subclasses.
 */
public abstract class Living implements Positionable, Mappable, TurnHandler {
  /**
   * Maximum health of the living object. Should naturally heal towards this
   * value each turn.
   */
  private int baseHealth;
  /**
   * Current health of the living object. If this value reaches 0, the living
   * object should be considered dead.
   */
  private int health;

  /**
   * Constructor for a new living object. Automatically sets the current health
   * to the maximum.
   *
   * @param baseHealth maximum health of this living object
   */
  public Living(int baseHealth) {
    this(baseHealth, baseHealth);
  }

  /**
   * Constructor for a new living object that doesn't necessarily have maximum
   * health.
   *
   * @param baseHealth maximum health of this living object
   * @param health     current health of this living object
   */
  public Living(int baseHealth, int health) {
    this.baseHealth = baseHealth;
    this.health = health;
  }

  /**
   * Increases a living's health by the specified amount up to the maximum
   * health
   *
   * @param healing amount to increase the health by
   */
  @SuppressWarnings("WeakerAccess")
  public final void heal(int healing) {
    setHealth(health + healing);
  }

  /**
   * Checks if the living's health is below its maximum and heals it up to 5
   * health if it is.
   *
   * @return true if the living needed healing
   */
  public final boolean naturalHeal() {
    // Check if healing is needed and heal up to 5 health if it is
    if (health < baseHealth) {
      heal(5);
      // Mark as healed
      return true;
    }
    return false;
  }

  /**
   * Decreases a living's health by the specified amount
   *
   * @param damage amount to decrease the health by
   */
  public final void damage(int damage) {
    this.health -= damage;
  }

  /**
   * Checks if the living is dead (i.e. the health is less than or equal to 0)
   *
   * @return whether the living is dead
   */
  public final boolean isDead() {
    return this.health <= 0;
  }

  /**
   * Sets the units maximum health. This will also set the health so that it's
   * the same proportion of health as it was before.
   *
   * @param baseHealth new maximum health
   */
  public final void setBaseHealth(int baseHealth) {
    // Get the old proportion
    double percentOfBase = getHealthPercent();
    this.baseHealth = baseHealth;
    // Ensure the proportion remains the same
    this.health = (int) Math.ceil(percentOfBase * (double) this.baseHealth);
  }

  /**
   * Gets the current health of the living
   *
   * @return current health
   */
  public int getHealth() {
    return health;
  }

  /**
   * Sets the current health of the living, making sure it doesn't exceed the
   * maximum
   *
   * @param health new current health
   */
  public void setHealth(int health) {
    this.health = health;
    // Check if the health exceeds the maximum and set it to the maximum if it
    // does
    if (this.health > this.baseHealth) {
      this.health = this.baseHealth;
    }
  }

  /**
   * Gets the maximum health of the living object
   *
   * @return maximum health of the living object
   */
  public int getBaseHealth() {
    return baseHealth;
  }

  /**
   * Gets the percent health filled of the living
   *
   * @return percent health filled
   */
  public double getHealthPercent() {
    return (double) health / (double) baseHealth;
  }

  /**
   * Stores health information in a map so it can be saved/sent over the
   * network. This should be overridden by subclasses to add their additional
   * information.
   *
   * @return map containing health information
   */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();

    // Store health information
    map.put("baseHealth", baseHealth);
    map.put("health", health);

    return map;
  }

  /**
   * Handle a unit attacking this living object. Should be overridden in
   * subclasses to describe how attacking units should be affected.
   *
   * @param attacker the unit attacking this living object
   * @param ranged   whether this was a ranged attack
   */
  public abstract void onAttacked(Unit attacker, boolean ranged);

  /**
   * Gets the living's owner. Should be overridden in subclasses.
   *
   * @return the living's owner
   */
  public abstract Player getOwner();

  /**
   * Gets the living's position in the map. Should be overridden in subclasses.
   *
   * @return the living's position in the map
   */
  public abstract Point2D getPosition();
}
