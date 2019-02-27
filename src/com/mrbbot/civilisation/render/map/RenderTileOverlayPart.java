package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.geometry.Hexagon;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

/**
 * Render object for one part of the tile overlay. One part represents one edge
 * of a hexagon.
 */
@ClientOnly
class RenderTileOverlayPart extends Render {
  /**
   * The wall part. Used for selections/path highlighting and city walls.
   */
  private Box wall;
  /**
   * The join between segments of walls in a city.
   */
  private Cylinder join;

  /**
   * Creates a new tile part
   *
   * @param angle  pivot angle for the part
   * @param colour initial colour of the part
   */
  RenderTileOverlayPart(double angle, Color colour) {
    super();

    // Create and pivot the wall
    Render wallHolder = new Render();
    wallHolder.rotateZ.setAngle(angle);
    wallHolder.translate.setY(Hexagon.SQRT_3 / 2);
    wall = new Box(1, 0.2, 0.1);
    wall.setMaterial(new PhongMaterial(colour));
    wall.setVisible(false);
    wall.setTranslateY(-0.1);
    wall.setTranslateZ(0.05);
    wallHolder.add(wall);

    // Create and pivot the join
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

  /**
   * Sets the visibility of the wall component of this part
   *
   * @param visible whether the wall should be visible
   */
  void setWallVisible(boolean visible) {
    wall.setVisible(visible);
  }

  /**
   * Sets the visibility of the join component of this part
   *
   * @param visible whether the join should be visible
   */
  void setJoinVisible(boolean visible) {
    join.setVisible(visible);
  }

  /**
   * Sets the colour of the wall component of this part
   *
   * @param colour new colour for the wall component
   */
  void setWallColour(Color colour) {
    wall.setMaterial(new PhongMaterial(colour));
  }

  /**
   * Sets the colour of the join component of this part
   *
   * @param colour new colour for the join component
   */
  void setJoinColour(Color colour) {
    join.setMaterial(new PhongMaterial(colour));
  }

  /**
   * Sets height of the wall/join components
   *
   * @param height             new height of the wall
   * @param tileHeight         height of the tile this overlay represents
   * @param greatestTileHeight greatest tile height of all tiles in the
   *                           containing city
   */
  void setWallHeight(
    double height,
    double tileHeight,
    double greatestTileHeight
  ) {
    wall.setDepth(height);
    wall.setTranslateZ(height / 2);

    // Update the join height
    updateJoinHeight(tileHeight, greatestTileHeight);
  }

  /**
   * Updates the join height so that it reaches the bottom of the ground
   *
   * @param tileHeight         height of the tile this overlay represents
   * @param greatestTileHeight greatest tile height of all tiles in the
   *                           containing city
   */
  private void updateJoinHeight(double tileHeight, double greatestTileHeight) {
    // Height of this join
    double joinHeight = greatestTileHeight + 0.4;

    join.setHeight(joinHeight);
    // Translate the join so that it has the same height and vertical position
    // for each tile in the containing city
    join.setTranslateZ(-tileHeight + (joinHeight / 2));
  }
}
