package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.geometry.Hexagon;
import com.mrbbot.generic.render.Render;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

class RenderTileOverlayPart extends Render {
  private Box wall;
  private Cylinder join;

  RenderTileOverlayPart(double angle, Color colour) {
    super();

    Render wallHolder = new Render();
    wallHolder.rotateZ.setAngle(angle);
    wallHolder.translate.setY(Hexagon.SQRT_3 / 2);
    wall = new Box(1, 0.2, 0.1);
    wall.setMaterial(new PhongMaterial(colour));
    wall.setVisible(false);
    wall.setTranslateY(-0.1);
    wall.setTranslateZ(0.05);
    wallHolder.add(wall);

    Render joinHolder = new Render();
    joinHolder.rotateZ.setAngle(angle + 30);
    joinHolder.translate.setY(1);
    join = new Cylinder(0.2, 0.1);
    join.setMaterial(new PhongMaterial(colour));
    join.setRotationAxis(Rotate.X_AXIS);
    join.setRotate(90);
    join.setTranslateZ(0.05);
    join.setVisible(false);

    joinHolder.add(join);

    add(wallHolder);
    add(joinHolder);
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

  void setWallColour(Color colour) {
    wall.setMaterial(new PhongMaterial(colour));
  }

  void setJoinColour(Color colour) {
    join.setMaterial(new PhongMaterial(colour));
  }

  void setWallHeight(double height, double tileHeight, double greatestTileHeight) {
    double halfHeight = height / 2;

    wall.setDepth(height);
    wall.setTranslateZ(halfHeight);

    updateJoinHeight(tileHeight, greatestTileHeight);
//
//    join.setHeight(height);
//    join.setTranslateZ(halfHeight);
  }

  private void updateJoinHeight(double tileHeight, double greatestTileHeight) {
    double joinHeight = greatestTileHeight + 0.4;

    join.setHeight(joinHeight);
    join.setTranslateZ(-tileHeight + (joinHeight / 2));

//    System.out.println(tileHeight);
    //join.setTranslateZ(tileHeight / 2);
  }
}
