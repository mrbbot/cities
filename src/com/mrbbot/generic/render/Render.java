package com.mrbbot.generic.render;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Base render object class with default transformations that can be adjusted
 * as required. Extends group so more nodes can be added as children.
 */
public class Render
  extends Group {
  /**
   * Scale transform for this render object.
   */
  public Scale scale = new Scale();
  /**
   * Transform for rotations in the X-axis
   */
  public Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
  /**
   * Transform for rotations in the Y-axis
   */
  public Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
  /**
   * Transform for rotations in the Z-axis
   */
  public Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
  /**
   * Transform for translations
   */
  public Translate translate = new Translate();

  public Render() {
    super();
    // Add the transforms so that translations are applied last (means they
    // won't affect scaling and rotation)
    getTransforms().addAll(scale, rotateX, rotateY, rotateZ, translate);
  }

  /**
   * Adds child nodes to this render object
   *
   * @param elements nodes to add
   */
  public final void add(Node... elements) {
    getChildren().addAll(elements);
  }

  /**
   * Removes child nodes from this render object
   *
   * @param elements nodes to remove
   */
  public final void remove(Node... elements) {
    getChildren().removeAll(elements);
  }

  /**
   * Resets all the transformations of this render object to their default
   * state.
   */
  public final void reset() {
    translateTo(0, 0, 0);
    rotateTo(0, 0, 0);
    scaleTo(1);
  }

  /**
   * Translates this render object to the specified coordinates.
   *
   * @param x new x-coordinate for this render object
   * @param y new y-coordinate for this render object
   * @param z new z-coordinate for this render object
   */
  public final void translateTo(double x, double y, double z) {
    translate.setX(x);
    translate.setY(y);
    translate.setZ(z);
  }

  /**
   * Rotates this render object to the specified angles in each axis
   *
   * @param xDegrees new x rotation in degrees for this render object
   * @param yDegrees new y rotation in degrees for this render object
   * @param zDegrees new z rotation in degrees for this render object
   */
  public final void rotateTo(
    double xDegrees,
    double yDegrees,
    double zDegrees
  ) {
    rotateX.setAngle(xDegrees);
    rotateY.setAngle(yDegrees);
    rotateZ.setAngle(zDegrees);
  }

  /**
   * Scales all axis of this render object to the same factor
   *
   * @param v new scale factor for all axis of this render object
   */
  public final void scaleTo(double v) {
    scaleTo(v, v, v);
  }

  /**
   * Scales the render object to the specified scale factors for each axis
   *
   * @param x new scale factor for the x-axis
   * @param y new scale factor for the y-axis
   * @param z new scale factor for the z-axis
   */
  public final void scaleTo(double x, double y, double z) {
    scale.setX(x);
    scale.setY(y);
    scale.setZ(z);
  }

  /**
   * Translates the render object by the specified amount, adding to the
   * existing values for translation.
   *
   * @param x increase in the x-axis translation amount
   * @param y increase in the y-axis translation amount
   * @param z increase in the z-axis translation amount
   */
  public final void translateBy(double x, double y, double z) {
    // Add to the existing values
    translate.setX(translate.getX() + x);
    translate.setY(translate.getY() + y);
    translate.setZ(translate.getZ() + z);
  }

  /**
   * Rotates the render object by the specified number of degrees in each axis,
   * adding to the existing values for rotation.
   *
   * @param xDegrees degrees increase in the x-rotation of this object
   * @param yDegrees degrees increase in the y-rotation of this object
   * @param zDegrees degrees increase in the z-rotation of this object
   */
  public final void rotateBy(
    double xDegrees,
    double yDegrees,
    double zDegrees
  ) {
    // Add to the existing values
    rotateX.setAngle(rotateX.getAngle() + xDegrees);
    rotateY.setAngle(rotateY.getAngle() + yDegrees);
    rotateZ.setAngle(rotateZ.getAngle() + zDegrees);
  }

}

