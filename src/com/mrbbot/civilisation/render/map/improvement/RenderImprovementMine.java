package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.List;
import java.util.Map;

/**
 * Render object for a mine improvement. Added to a {@link RenderImprovement}.
 */
@ClientOnly
public class RenderImprovementMine extends Render {
  /**
   * Various colours the rocks can be
   */
  private static final Color[] ROCK_COLOURS = new Color[]{
    Color.WHITESMOKE,
    Color.ORANGERED,
    Color.DIMGREY.darker().darker()
  };

  @SuppressWarnings("unchecked")
  RenderImprovementMine(Map<String, Object> metadata) {
    // Get the rock sizes and colours
    List<Double> sizes = (List<Double>) metadata.get("sizes");
    List<Integer> colours = (List<Integer>) metadata.get("colours");

    // Create the 3 rocks
    for (int i = 0; i < 3; i++) {
      double size = sizes.get(i);
      int colour = colours.get(i);
      int angle = i * 120;
      add(makeRock(size, colour, angle));
    }
  }

  /**
   * Makes a rock for the mine render
   *
   * @param size  relative size of the rock
   * @param color colour index of the rock
   * @param angle angle the rock should be pivoted by
   * @return render object containing the rock
   */
  private Render makeRock(double size, int color, int angle) {
    // Create the rock
    Box rock = new Box(
      size / 2.0,
      size / 2.0,
      size / 2.0
    );
    rock.setMaterial(new PhongMaterial(ROCK_COLOURS[color]));
    rock.setTranslateZ(size / 4.0);

    // Add it to a render object and pivot it the specified number of degrees
    Render rockHolder = new Render();
    rockHolder.rotateZ.setAngle(angle);
    rockHolder.translate.setX(0.5);
    rockHolder.add(rock);
    return rockHolder;
  }
}
