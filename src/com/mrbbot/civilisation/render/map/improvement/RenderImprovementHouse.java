package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;

/**
 * Render object for a capital improvement. Added to a
 * {@link RenderImprovement}.
 */
@ClientOnly
public class RenderImprovementHouse extends Render {
  /**
   * Roof colour of a default house
   */
  private static final Color ROOF_COLOUR = Color.BROWN.darker().darker();
  /**
   * Wall colour of a default house
   */
  private static final Color WALL_COLOUR = Color.GOLDENROD;

  RenderImprovementHouse(Color colour) {
    // Create and add the walls
    Box box = new Box(0.5, 0.5, 0.5);
    box.setTranslateZ(0.25);
    box.setMaterial(new PhongMaterial(colour == null ? WALL_COLOUR : colour));
    add(box);

    // Create and add the roof (triangular prism, cylinder with 3 divisions)
    Cylinder roof = new Cylinder(0.5, 0.7, 3);
    roof.setTranslateZ(0.25 + 0.5);
    roof.setMaterial(new PhongMaterial(
      colour == null
        ? ROOF_COLOUR
        : colour.darker())
    );
    add(roof);
  }
}
