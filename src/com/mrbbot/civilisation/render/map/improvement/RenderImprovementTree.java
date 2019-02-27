package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Render object for a tree on the map. Added to a {@link RenderImprovement}.
 */
public class RenderImprovementTree extends Render {
  /**
   * Material used for rendering a tree's logs
   */
  private static final Material LOG_MATERIAL =
    new PhongMaterial(Color.BROWN.darker());
  /**
   * Material used for rendering a tree's bushes
   */
  private static final Material BUSH_MATERIAL =
    new PhongMaterial(Color.FORESTGREEN);
  /**
   * Height of a trees trunk
   */
  private static final double TREE_HEIGHT = 0.6;

  RenderImprovementTree() {
    // Create the tree trunk
    Cylinder log = new Cylinder(0.2, TREE_HEIGHT);
    log.getTransforms().addAll(
      new Rotate(90, Rotate.X_AXIS),
      new Translate(0, TREE_HEIGHT / 2, 0)
    );
    log.setMaterial(LOG_MATERIAL);

    // Create the tree's leaves
    Sphere bush = new Sphere(0.4);
    bush.getTransforms().add(
      new Translate(0, 0, TREE_HEIGHT + 0.2)
    );
    bush.setMaterial(BUSH_MATERIAL);

    getChildren().addAll(log, bush);
  }
}
