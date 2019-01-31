package com.mrbbot.civilisation.geometry;

import com.mrbbot.civilisation.net.serializable.SerializablePoint2D;
import javafx.geometry.Point2D;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

import java.io.Serializable;

public class Hexagon implements Serializable {
    public static final double SQRT_3 = Math.sqrt(3);

    private SerializablePoint2D c;
    private double r;
    private SerializablePoint2D[] vertices;

    public Hexagon(SerializablePoint2D center, double radius) {
        this.c = center;
        this.r = radius /*- 0.1*/; // - 0.1 puts a gap in between hexes
        calculateVertices();
    }

    private void calculateVertices() {
        double cx = c.getX();       // alias for c.getX()
        double cy = c.getY();       // alias for c.getY()
        double hr = r / 2;          // half radius
        double hw = SQRT_3 * hr;    // half width

        this.vertices = new SerializablePoint2D[]{
                new SerializablePoint2D(cx, cy - r),
                new SerializablePoint2D(cx - hw, cy - hr),
                new SerializablePoint2D(cx - hw, cy + hr),
                new SerializablePoint2D(cx, cy + r),
                new SerializablePoint2D(cx + hw, cy + hr),
                new SerializablePoint2D(cx + hw, cy - hr),
        };
    }

    public SerializablePoint2D[] getVertices() {
        return vertices;
    }

    public Shape3D getPrism(double height) {
        Cylinder cylinder = new Cylinder(r, height, 6);
        cylinder.getTransforms().addAll(
                new Rotate(90, Rotate.X_AXIS));
        return cylinder;
    }

    public SerializablePoint2D getCenter() {
        return c;
    }

    public void setCenter(SerializablePoint2D center) {
        this.c = center;
        calculateVertices();
    }

    public double getRadius() {
        return r;
    }

    public void setRadius(double radius) {
        this.r = radius;
        calculateVertices();
    }

    @Override
    public String toString() {
        return "Hexagon [ cx = " + c.getX() + ", cy = " + c.getY() + ", r = " + r + "]";
    }
}
