package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;

/**
 * Render object for a road improvement. Added to a {@link RenderImprovement}.
 */
public class RenderImprovementRoad extends Render {
  /**
   * Colour of road segments
   */
  private static final Color ROAD_COLOUR = Color.DIMGREY.darker();
  /**
   * Height of the road off the ground
   */
  private static final double ROAD_HEIGHT = 0.075;
  /**
   * Width of road segments
   */
  private static final double ROAD_WIDTH = 0.3;
  /**
   * Length of road segments, from the center of tiles to the edges
   */
  private static final double ROAD_LENGTH = Math.sqrt(0.75);

  RenderImprovementRoad(Tile thisTile, ArrayList<Tile> adjacentTiles) {
    // Adds the center join which connects road road segments together or just
    // indicates the tile has a road if there are not adjacent connections
    Cylinder join = new Cylinder(
      ROAD_WIDTH / 2,
      ROAD_HEIGHT,
      6
    );
    join.setMaterial(new PhongMaterial(ROAD_COLOUR));
    join.setTranslateZ(ROAD_HEIGHT / 2);
    join.setRotationAxis(Rotate.X_AXIS);
    join.setRotate(90);
    add(join);

    // For every adjacent tile, checks if there is a connecting road
    for (Tile adjacentTile : adjacentTiles) {
      if (adjacentTile.hasRoad()) {
        int dx = adjacentTile.x - thisTile.x;
        int dy = adjacentTile.y - thisTile.y;

        // Calculates the pivot angle of the road segment
        int angle = 0;
        int xOffset = thisTile.y % 2;
        if (dy == 0) {
          angle = dx == 1 ? 0 : 60 * 3;
        } else if (dy == 1) {
          angle = dx == -xOffset ? 60 * 4 : 60 * 5;
        } else if (dy == -1) {
          angle = dx == -xOffset ? 60 * 2 : 60;
        }

        // Calculates the height difference for the road connector
        double heightDifference =
          adjacentTile.getHeight() - thisTile.getHeight();

        // Adds the road segment for this connection
        add(buildRoadSegment(angle, heightDifference));
      }
    }
  }

  /**
   * Creates a segment of road to be added to this improvement
   *
   * @param angle            pivot angle of this segment
   * @param heightDifference difference in height between this and the
   *                         connecting road
   * @return render object containing the road segment
   */
  @SuppressWarnings("Duplicates")
  private Render buildRoadSegment(double angle, double heightDifference) {
    // Build the render object and pivot it
    Render rotor = new Render();
    rotor.rotateZ.setAngle(angle);

    //
    //      #-----
    //      #
    // =====#
    //

    // Build the actual segment of road (the ='s in the above diagram)
    Box road = new Box(ROAD_WIDTH, ROAD_LENGTH, ROAD_HEIGHT);
    road.setMaterial(new PhongMaterial(ROAD_COLOUR));
    road.setTranslateX(ROAD_LENGTH / 2);
    road.setTranslateZ(ROAD_HEIGHT / 2);
    road.setRotationAxis(Rotate.Z_AXIS);
    road.setRotate(90);
    rotor.add(road);

    // Only render joins that go up in height
    double roadJoinHeight = Math.max(heightDifference, 0) + ROAD_HEIGHT;
    // Add the box that joins the road segment on this tile and the road
    // segment on the adjacent tile (the #'s in the above diagram)
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
