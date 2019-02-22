package com.mrbbot.civilisation.logic;

import com.mrbbot.civilisation.logic.interfaces.Mappable;
import com.mrbbot.civilisation.logic.interfaces.Positionable;
import com.mrbbot.civilisation.logic.interfaces.TurnHandler;
import com.mrbbot.civilisation.logic.unit.Unit;
import javafx.geometry.Point2D;

import java.util.HashMap;
import java.util.Map;

public abstract class Living implements Positionable, Mappable, TurnHandler {
  private int baseHealth;
  private int health;

  public Living(int baseHealth) {
    this(baseHealth, baseHealth);
  }

  public Living(int baseHealth, int health) {
    this.baseHealth = baseHealth;
    this.health = health;
  }

  public final void heal(int healing) {
    setHealth(health + healing);
  }

  public final boolean naturalHeal() {
    if (health < baseHealth) {
      heal(5);
      return true;
    }
    return false;
  }

  public final void damage(int damage) {
    this.health -= damage;
  }

  public final boolean isDead() {
    return this.health <= 0;
  }

  public final void setBaseHealth(int baseHealth) {
    double percentOfBase = (double)this.health / (double)this.baseHealth;
    this.baseHealth = baseHealth;
    this.health = (int) Math.ceil(percentOfBase * (double)this.baseHealth);
  }

  public int getHealth() {
    return health;
  }

  public void setHealth(int health) {
    this.health = health;
    if(this.health > this.baseHealth) {
      this.health = this.baseHealth;
    }
  }

  public int getBaseHealth() {
    return baseHealth;
  }

  public double getHealthPercent() {
    return (double)health / (double)baseHealth;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();

    map.put("baseHealth", baseHealth);
    map.put("health", health);

    return map;
  }

  public abstract void onAttack(Unit attacker, boolean ranged);

  public abstract Player getOwner();

  public abstract Point2D getPosition();
}
