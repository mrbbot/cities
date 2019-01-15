package com.mrbbot.generic.render;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

@SuppressWarnings("WeakerAccess")
public class Render
  extends Group {
  public Scale scale = new Scale();
  public Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
  public Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
  public Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
  public Translate translate = new Translate();

  public Render() {
    super();
    getTransforms().addAll(scale, rotateX, rotateY, rotateZ, translate);
  }

  public final void add(Node... elements) {
    getChildren().addAll(elements);
  }

  public final void remove(Node... elements) {
    getChildren().removeAll(elements);
  }

  public final void translateTo(double x, double y, double z) {
    translate.setX(x);
    translate.setY(y);
    translate.setZ(z);
  }

  public final void rotateTo(
    double xDegrees,
    double yDegrees,
    double zDegrees
  ) {
    rotateX.setAngle(xDegrees);
    rotateY.setAngle(yDegrees);
    rotateZ.setAngle(zDegrees);
  }

  public final void translateBy(double x, double y, double z) {
    translate.setX(translate.getX() + x);
    translate.setY(translate.getY() + y);
    translate.setZ(translate.getZ() + z);
  }

  public final void rotateBy(
    double xDegrees,
    double yDegrees,
    double zDegrees
  ) {
    rotateX.setAngle(rotateX.getAngle() + xDegrees);
    rotateY.setAngle(rotateY.getAngle() + yDegrees);
    rotateZ.setAngle(rotateZ.getAngle() + zDegrees);
  }

}

