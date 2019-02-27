package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Render object for a farm improvement. Added to a {@link RenderImprovement}.
 */
@ClientOnly
public class RenderImprovementFarm extends Render {
  /**
   * Width/length of the farm
   */
  private static final double SIZE = 0.7;
  /**
   * Colour of the fence around the farm
   */
  private static final Color FENCE_COLOUR = Color.BROWN.darker().darker();
  /**
   * Colour of the grass strips in the farm
   */
  private static final Color GRASS_COLOUR = Color.GREEN;
  /**
   * Colour of the soil strips in the farm
   */
  private static final Color SOIL_COLOUR = Color.BROWN.darker();

  RenderImprovementFarm(Map<String, Object> metadata) {
    // Get the number of alternating strips this farm has
    double numStrips = (int) metadata.get("strips");

    // Calculate the strips' size and position
    double stripSize = SIZE / numStrips;
    double startTranslate = -(numStrips - 1) / 2.0 * stripSize;

    // Add the strips
    for (int i = 0; i < numStrips; i++) {
      Box strip = new Box(stripSize, SIZE, 0.1);
      strip.setTranslateX(startTranslate + (i * stripSize));
      strip.setMaterial(new PhongMaterial(i % 2 == 0 ? GRASS_COLOUR : SOIL_COLOUR));
      add(strip);
    }

    // Add the fences around the farm
    add(makeWall(0));
    add(makeWall(90));
    add(makeWall(180));
    add(makeWall(270));

    // Shift the farm up and rotate it by the set angle
    translate.setZ(0.05);
    rotateZ.setAngle((int) metadata.get("angle"));
  }

  /**
   * Makes a segment of the wall
   * @param angle angle to pivot the wall by in degrees
   * @return render object containing the wall
   */
  private Render makeWall(double angle) {
    // Create a render object used to pivot the wall around
    Render wallHolder = new Render();
    wallHolder.rotateZ.setAngle(angle);

    // Add the wall
    Box box = new Box(0.1, SIZE + 0.2, 0.15);
    box.setTranslateX((SIZE / 2) + 0.05);
    box.setTranslateZ(0.05);
    box.setMaterial(new PhongMaterial(FENCE_COLOUR));
    wallHolder.add(box);

    return wallHolder;
  }
}
