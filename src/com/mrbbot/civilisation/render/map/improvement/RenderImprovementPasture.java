package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

@ClientOnly
public class RenderImprovementPasture extends Render {
  private static final double SIZE = 0.4;
  private static final Color FENCE_COLOUR = Color.BROWN.darker().darker();

  RenderImprovementPasture() {
    add(makeFence(0));
    add(makeFence(90));
    add(makeFence(180));
    add(makeFence(270));

    //translate.setZ(0.05);
  }

  private Render makeFence(double angle) {
    Render fenceHolder = new Render();
    fenceHolder.translate.setX(0.3);
    fenceHolder.rotateZ.setAngle(angle + 45);

    Box fence = new Box(0.01, SIZE + 0.2, 0.1);
    fence.setMaterial(new PhongMaterial(FENCE_COLOUR));
    fence.setTranslateZ(0.1);
    Box fence2 = new Box(0.01, SIZE + 0.2, 0.1);
    fence2.setMaterial(new PhongMaterial(FENCE_COLOUR));
    fence2.setTranslateZ(0.25);
    Box fencePost = new Box(0.1, 0.1, 0.35);
    fencePost.setMaterial(new PhongMaterial(FENCE_COLOUR));
    fencePost.setTranslateZ(0.35 / 2.0);
    fencePost.setTranslateY((SIZE / 2.0) + 0.1);

    fenceHolder.add(fence);
    fenceHolder.add(fence2);
    fenceHolder.add(fencePost);

    return fenceHolder;
  }
}
