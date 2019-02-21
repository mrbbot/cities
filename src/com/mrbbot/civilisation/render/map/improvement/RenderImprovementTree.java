package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class RenderImprovementTree extends Render {
  private static final Material LOG_MATERIAL = new PhongMaterial(Color.BROWN.darker());
  private static final Material BUSH_MATERIAL = new PhongMaterial(Color.FORESTGREEN);
  private static final double TREE_HEIGHT = 0.6;

  RenderImprovementTree() {
    Cylinder log = new Cylinder(0.2, TREE_HEIGHT);
    log.getTransforms().addAll(
      new Rotate(90, Rotate.X_AXIS),
      new Translate(0, TREE_HEIGHT / 2, 0)
    );
    log.setMaterial(LOG_MATERIAL);

    Sphere bush = new Sphere(0.4);
    bush.getTransforms().add(
      new Translate(0, 0, TREE_HEIGHT + 0.2)
    );
    bush.setMaterial(BUSH_MATERIAL);

    getChildren().addAll(log, bush);
  }
}
