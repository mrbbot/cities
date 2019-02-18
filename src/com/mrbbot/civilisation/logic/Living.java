package com.mrbbot.civilisation.logic;

import com.mrbbot.civilisation.logic.interfaces.Mappable;
import com.mrbbot.civilisation.logic.interfaces.Positionable;

import java.util.HashMap;
import java.util.Map;

public abstract class Living implements Positionable, Mappable {
  public int baseHealth;
  public int health;

  public Living(int baseHealth) {
    this.baseHealth = baseHealth;
    this.health = baseHealth;
  }

  public final void heal(int healing) {
    this.health += healing;
    if(this.health > this.baseHealth) {
      this.health = this.baseHealth;
    }
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

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();

    map.put("baseHealth", baseHealth);
    map.put("health", health);

    return map;
  }
}
