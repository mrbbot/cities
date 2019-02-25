package com.mrbbot.civilisation.geometry;

import javafx.geometry.Point2D;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

/**
 * Class that represents a Hexagon in 2D space
 */
public class Hexagon {
  /**
   * The square root of 3, used in calculations of distances to edges
   */
  public static final double SQRT_3 = Math.sqrt(3);

  /**
   * The center of the hexagon
   */
  private Point2D c;
  /**
   * The radius from the center of the hexagon
   */
  private double r;
  /**
   * The points that join the edges of the hexagon. All of these points are a
   * {@link #r radius} from the {@link #c center}.
   */
  private Point2D[] vertices;

  /**
   * Creates a new hexagon using the specific parameters
   *
   * @param center center coordinate of the new hexagon
   * @param radius radius from the center of the new hexagon
   */
  Hexagon(Point2D center, double radius) {
    this.c = center;
    this.r = radius /*- 0.1*/; // - 0.1 puts a gap in between hexes
    calculateVertices();
  }

  /**
   * Calculates the vertices of the hexagon. This is only called when any of
   * the data required to calculate them changes.
   */
  private void calculateVertices() {
    double cx = c.getX();       // alias for c.getX()
    double cy = c.getY();       // alias for c.getY()
    double hr = r / 2;          // half radius
    double hw = SQRT_3 * hr;    // half width

    // Calculate the vertices and store them in a new array (the old one will
    // be garbage collected)
    this.vertices = new Point2D[]{
      new Point2D(cx, cy - r),
      new Point2D(cx - hw, cy - hr),
      new Point2D(cx - hw, cy + hr),
      new Point2D(cx, cy + r),
      new Point2D(cx + hw, cy + hr),
      new Point2D(cx + hw, cy - hr),
    };
  }

  /**
   * Gets the vertices of this hexagon
   *
   * @return vertices of this hexagon
   */
  public Point2D[] getVertices() {
    return vertices;
  }

  /**
   * Gets a hexagonal prism created by extruding the cross section
   *
   * @param height the height of the new prism
   * @return a 3D hexagonal prism
   */
  public Shape3D getPrism(double height) {
    Cylinder cylinder = new Cylinder(r, height, 6);
    cylinder.getTransforms().addAll(
      new Rotate(90, Rotate.X_AXIS));
    return cylinder;
  }

  /**
   * Gets the center of this hexagon
   *
   * @return center of this hexagon
   */
  public Point2D getCenter() {
    return c;
  }

  /**
   * Sets the center of this hexagon and then recalculates the vertices
   *
   * @param center new center of the hexagon
   */
  public void setCenter(Point2D center) {
    this.c = center;
    calculateVertices();
  }

  /**
   * Gets the radius of this hexagon
   *
   * @return radius of this hexagon
   */
  public double getRadius() {
    return r;
  }

  /**
   * Sets the radius of this hexagon and then recalculates the vertices
   *
   * @param radius new radius of the hexagon
   */
  public void setRadius(double radius) {
    this.r = radius;
    calculateVertices();
  }

  @Override
  public String toString() {
    return "Hexagon[cx = " + c.getX()
      + ", cy = " + c.getY() + ", r = " + r + "]";
  }
}
