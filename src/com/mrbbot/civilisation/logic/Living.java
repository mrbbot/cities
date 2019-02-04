package com.mrbbot.civilisation.logic;

import java.io.Serializable;

public abstract class Living implements Serializable {
  public int health;

  public Living(int baseHealth) {
    this.health = baseHealth;
  }

  public final void heal(int healing) {
    this.health += healing;
  }

  public final void damage(int damage) {
    this.health -= damage;
  }

  public final boolean isDead() {
    return this.health <= 0;
  }
}
