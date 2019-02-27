package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Render object for a pasture improvement. Added to a {@link RenderImprovement}.
 */
@ClientOnly
public class RenderImprovementPasture extends Render {
  /**
   * Width/length of the pasture
   */
  private static final double SIZE = 0.4;
  /**
   * Colours of the pasture fences
   */
  private static final Color FENCE_COLOUR = Color.BROWN.darker().darker();

  RenderImprovementPasture() {
    // Add the pasture fences
    add(makeFence(0));
    add(makeFence(90));
    add(makeFence(180));
    add(makeFence(270));
  }

  /**
   * Creates a fence render object and pivots it the specified number of
   * degrees
   *
   * @param angle angle to pivot the fence by in degrees
   * @return render object containing this fence segment and a fence corner
   * post
   */
  private Render makeFence(double angle) {
    // Create the holder render object and pivot it
    Render fenceHolder = new Render();
    fenceHolder.translate.setX(0.3);
    fenceHolder.rotateZ.setAngle(angle + 45);

    // Create the bottom fence
    Box fence = new Box(0.01, SIZE + 0.2, 0.1);
    fence.setMaterial(new PhongMaterial(FENCE_COLOUR));
    fence.setTranslateZ(0.1);
    // Create the top fence
    Box fence2 = new Box(0.01, SIZE + 0.2, 0.1);
    fence2.setMaterial(new PhongMaterial(FENCE_COLOUR));
    fence2.setTranslateZ(0.25);
    // Create the corner fence post
    Box fencePost = new Box(0.1, 0.1, 0.35);
    fencePost.setMaterial(new PhongMaterial(FENCE_COLOUR));
    fencePost.setTranslateZ(0.35 / 2.0);
    fencePost.setTranslateY((SIZE / 2.0) + 0.1);

    // Add the fence components
    fenceHolder.add(fence);
    fenceHolder.add(fence2);
    fenceHolder.add(fencePost);

    return fenceHolder;
  }
}
