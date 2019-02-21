package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;

public class RenderImprovementRoad extends Render {
  private static final Color ROAD_COLOUR = Color.DIMGREY.darker();
  private static final double ROAD_HEIGHT = 0.075;
  private static final double ROAD_WIDTH = 0.3;
  private static final double ROAD_LENGTH = Math.sqrt(0.75);

  RenderImprovementRoad(Tile thisTile, ArrayList<Tile> adjacentTiles) {
    Cylinder join = new Cylinder(ROAD_WIDTH / 2, ROAD_HEIGHT, 6);
    join.setMaterial(new PhongMaterial(ROAD_COLOUR));
    join.setTranslateZ(ROAD_HEIGHT / 2);
    join.setRotationAxis(Rotate.X_AXIS);
    join.setRotate(90);
    add(join);

    int count = 0;
    for (Tile adjacentTile : adjacentTiles) {
      if (adjacentTile.hasRoad()) {
        count++;
        int dx = adjacentTile.x - thisTile.x;
        int dy = adjacentTile.y - thisTile.y;
        /*System.out.println(dx);
        System.out.println(dy);*/

        int angle = 0;
        int xOffset = thisTile.y % 2;
        if (dy == 0) {
          angle = dx == 1 ? 0 : 60 * 3;
        } else if (dy == 1) {
          angle = dx == -xOffset ? 60 * 4 : 60 * 5;
        } else if (dy == -1) {
          angle = dx == -xOffset ? 60 * 2 : 60;
        }
        double heightDifference = adjacentTile.getHeight() - thisTile.getHeight();
        add(buildRoadSegment(angle, heightDifference));
      }
    }
  }

  @SuppressWarnings("Duplicates")
  private Render buildRoadSegment(double angle, double heightDifference) {
    Render rotor = new Render();
    rotor.rotateZ.setAngle(angle);

    Box road = new Box(ROAD_WIDTH, ROAD_LENGTH, ROAD_HEIGHT);
    road.setMaterial(new PhongMaterial(ROAD_COLOUR));
    road.setTranslateX(ROAD_LENGTH / 2);
    road.setTranslateZ(ROAD_HEIGHT / 2);
    road.setRotationAxis(Rotate.Z_AXIS);
    road.setRotate(90);
    rotor.add(road);

    double roadJoinHeight = Math.max(heightDifference, 0) + ROAD_HEIGHT;
    Box roadJoin = new Box(ROAD_WIDTH, ROAD_HEIGHT, roadJoinHeight);
    roadJoin.setMaterial(new PhongMaterial(ROAD_COLOUR));
    roadJoin.setTranslateX(ROAD_LENGTH - (ROAD_HEIGHT / 2));
    roadJoin.setTranslateZ(roadJoinHeight / 2);
    roadJoin.setRotationAxis(Rotate.Z_AXIS);
    roadJoin.setRotate(90);
    rotor.add(roadJoin);

    return rotor;
  }


}
