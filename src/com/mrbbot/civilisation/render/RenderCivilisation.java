package com.mrbbot.civilisation.render;

import com.mrbbot.civilisation.render.map.RenderGame;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.RenderRoot;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

@ClientOnly
public class RenderCivilisation extends RenderRoot<RenderGame> {
  private final static double MAX_ZOOM = 5;
  private final static double MIN_ZOOM = 80;
  private final static boolean ENABLE_LIGHTING = false;

  private double oldMouseX = -1, oldMouseY = -1;

  private PointLight sun, moon;
  private Rotate sunMoonRotate;

  public RenderCivilisation(RenderGame root, int width, int height) {
    super(root, width, height);

    if(ENABLE_LIGHTING) {
      sun = new PointLight(Color.WHITE);
      moon = new PointLight(Color.BLACK);
      sunMoonRotate = new Rotate(0, Rotate.Y_AXIS);
      sun.getTransforms().addAll(sunMoonRotate, new Translate(-20, 0, 0));
      moon.getTransforms().addAll(sunMoonRotate, new Translate(20, 0, 0));
      AmbientLight ambientLight = new AmbientLight(Color.color(0.1, 0.1, 0.1));
      getChildren().addAll(sun, moon, ambientLight);
      setTime(90);
      new Thread(this::runDayNightCycle, "DayNightCycle").start();
    }

    //
    // PANNING & ZOOMING
    //
    subScene.setOnScroll((e) -> {
      double newValue = camera.translate.getZ() + (e.getDeltaY() / 40);
      if (newValue > -MAX_ZOOM) newValue = -MAX_ZOOM;
      if (newValue < -MIN_ZOOM) newValue = -MIN_ZOOM;
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

  public void setScene(Scene scene, EventHandler<? super KeyEvent> eventHandler) {
    scene.setOnKeyPressed((e) -> {
      switch(e.getCode()) {
        case W:
          camera.rotateBy(-1, 0, 0);
          break;
        case S:
          camera.rotateBy(1, 0, 0);
          break;
        case R:
          root.updateTileRenders();
          System.out.println("Updated tile renders");
          break;
      }
      eventHandler.handle(e);
    });
  }

  private void runDayNightCycle() {
    try {
      while (true) {
        double second = (System.currentTimeMillis() / 1000.0) % 60.0;

        Platform.runLater(() -> setTime(second * 6));
        Thread.sleep(1000 / 30);
      }
    } catch (InterruptedException ignored) {}
  }

  //sunrise(0) -> midday (90) -> sunset(180) -> midnight (270) -> sunrise (360)
  //0 <= angle <= 360
  private void setTime(double angle) {
    sunMoonRotate.setAngle(angle);

    double sunValue = Math.max(Math.sin(Math.toRadians(angle)), 0);
    double moonValue = (1 - sunValue) / 4;

    Color sunColour = Color.color(sunValue, sunValue, sunValue * 0.95);
    Color moonColour = Color.color(moonValue, moonValue, moonValue * 2);

    sun.setColor(sunColour);
    moon.setColor(moonColour);

    subScene.setFill(sunColour);
  }
}
