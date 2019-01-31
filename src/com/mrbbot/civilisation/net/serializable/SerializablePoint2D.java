package com.mrbbot.civilisation.net.serializable;

import javafx.geometry.Point2D;

import java.io.*;

public class SerializablePoint2D implements Serializable {
  public Point2D point;

  public SerializablePoint2D(double x, double y) {
    point = new Point2D(x, y);
  }

  public double getX() {
    return point.getX();
  }

  public double getY() {
    return point.getY();
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeDouble(getX());
    out.writeDouble(getY());
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    point = new Point2D(in.readDouble(), in.readDouble());
  }
}
