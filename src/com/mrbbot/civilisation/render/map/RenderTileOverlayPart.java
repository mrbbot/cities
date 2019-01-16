package com.mrbbot.civilisation.render.map;

import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

class RenderTileOverlayPart extends Render {
  private Box wall;
  private Cylinder join;

  RenderTileOverlayPart(Color colour) {
    super();

    wall = new Box(1, 0.2, 0.1);
    wall.setMaterial(new PhongMaterial(colour));
    wall.setVisible(false);
    wall.setTranslateZ(0.05);

    join = new Cylinder(0.2, 0.1);
    join.setRotate(90);
    join.setTranslateZ(0.05);
    join.setTranslateX(0.5);
    join.setRotationAxis(Rotate.X_AXIS);

    add(wall);
    add(join);
  }

  boolean isWallVisible() {
    return wall.isVisible();
  }

  boolean isJoinVisible() {
    return join.isVisible();
  }

  void setWallVisible(boolean visible) {
    wall.setVisible(visible);
  }

  void setJoinVisible(boolean visible) {
    join.setVisible(visible);
  }

  void setPartColour(Color colour) {
    wall.setMaterial(new PhongMaterial(colour));
    join.setMaterial(new PhongMaterial(colour));
  }

  void setPartHeight(double height) {
    double halfHeight = height / 2;

    wall.setDepth(height);
    wall.setTranslateZ(halfHeight);

    join.setHeight(height);
    join.setTranslateZ(halfHeight);
  }
}
