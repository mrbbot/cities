package com.mrbbot.civilisation.net.serializable;

import java.io.Serializable;

public class SerializableIntPoint2D implements Serializable {
  public int x, y;

  public SerializableIntPoint2D(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
