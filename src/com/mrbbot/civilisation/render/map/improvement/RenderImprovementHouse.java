package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;

public class RenderImprovementHouse extends Render {
  private static final Color ROOF_COLOUR = Color.BROWN.darker().darker();
  private static final Color WALL_COLOUR = Color.GOLDENROD;

  RenderImprovementHouse(Color colour) {
    Box box = new Box(0.5, 0.5, 0.5);
    box.setTranslateZ(0.25);
    box.setMaterial(new PhongMaterial(colour == null ? WALL_COLOUR : colour));
    add(box);

    Cylinder roof = new Cylinder(0.5, 0.7, 3);
    roof.setTranslateZ(0.25 + 0.5);
    roof.setMaterial(new PhongMaterial(colour == null ? ROOF_COLOUR : colour.darker()));
    add(roof);
  }
}
