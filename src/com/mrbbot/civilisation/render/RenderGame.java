package com.mrbbot.civilisation.render;

import com.mrbbot.civilisation.render.map.RenderMap;
import com.mrbbot.generic.render.RenderRoot;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;

public class RenderGame extends RenderRoot<RenderMap> {
  private double oldMouseX = -1, oldMouseY = -1;

  public RenderGame(RenderMap root, int width, int height) {
    super(root, width, height);

    /*PointLight light = new PointLight(Color.WHITE);
    light.getTransforms().add(new Translate(0, 0, 5));
    add(light);*/

    //
    // PANNING & ZOOMING
    //
    subScene.setOnScroll((e) -> {
      double newValue = camera.translate.getZ() + (e.getDeltaY() / 40);
      if (newValue > -10) newValue = -10;
      if (newValue < -30) newValue = -30;
      camera.translate.setZ(newValue);
    });

    subScene.setOnMouseDragged((e) -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        double x = e.getX(), y = e.getY();
        if (oldMouseX == -1 || oldMouseY == -1) {
          oldMouseX = x;
          oldMouseY = y;
        } else {
          double dX = x - oldMouseX;
          double dY = y - oldMouseY;
          oldMouseX = x;
          oldMouseY = y;

          double multiplier = (Math.abs(camera.translate.getZ()) / 30) * 2;

          dX *= multiplier;
          dY *= multiplier;

          camera.translateBy(-dX / 250, -dY / 250, 0);
        }
      }
    });

    subScene.setOnMouseReleased((e) -> {
      switch(e.getButton()) {
        case PRIMARY:
          oldMouseX = -1;
          oldMouseY = -1;
          break;
        case SECONDARY:
          this.root.resetPathfinding();
          break;
        default:
          break;
      }
    });
  }

  public void setScene(Scene scene) {
    scene.setOnKeyPressed((e) -> {
      switch(e.getCode()) {
        case W:
          camera.rotateBy(-1, 0, 0);
          break;
        case S:
          camera.rotateBy(1, 0, 0);
          break;
      }
    });
  }
}
