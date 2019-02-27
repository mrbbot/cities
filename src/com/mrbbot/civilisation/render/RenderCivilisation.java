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

/**
 * Render object containing the game render that handles zooming, panning and
 * lighting. This is the root render object, so extends {@link RenderRoot} in
 * order to access methods that create a sub-scene that can be included in the
 * JavaFX application.
 */
@ClientOnly
public class RenderCivilisation extends RenderRoot<RenderGame> {
  /**
   * Most zoomed in scale.
   */
  private final static double MAX_ZOOM = 5;
  /**
   * Most zoomed out scale.
   */
  private final static double MIN_ZOOM = 80;
  /**
   * Easy way to enable/disable lighting. Used for development as lighting can
   * be quite annoying.
   */
  private final static boolean ENABLE_LIGHTING = false;

  /**
   * Last x-coordinate of drag. Used for calculating how much the screen was
   * dragged.
   */
  private double oldMouseX = -1;
  /**
   * Last y-coordinate of drag. Used for calculating how much the screen was
   * dragged.
   */
  private double oldMouseY = -1;

  /**
   * Point light source representing the sun. Rotated round the map to simulate
   * day/night.
   */
  private PointLight sun;
  /**
   * Point light source representing the moon. Rotated round the map to
   * simulate day/night.
   */
  private PointLight moon;
  /**
   * Rotate transform applied to both the sun and moon to simulate day/night.
   */
  private Rotate sunMoonRotate;

  /**
   * Creates a new instance of the root render object.
   *
   * @param root   root game render to show
   * @param width  width of the screen
   * @param height height of the screen
   */
  public RenderCivilisation(RenderGame root, int width, int height) {
    // Pass width/height to super constructor so an appropriately sized sub-
    // scene can be created.
    super(root, width, height);

    // Setup lighting if required
    if (ENABLE_LIGHTING) {
      // Create new lights for the sun/moon
      sun = new PointLight(Color.WHITE);
      moon = new PointLight(Color.BLACK);

      // Create and add transforms for the sun and the moon. Notice the moon
      // has a negative x-transform so it will always be on the opposite side
      // to the sun.
      sunMoonRotate = new Rotate(0, Rotate.Y_AXIS);
      sun.getTransforms().addAll(
        sunMoonRotate, new Translate(-20, 0, 0)
      );
      moon.getTransforms().addAll(
        sunMoonRotate, new Translate(20, 0, 0)
      );

      // Create an ambient light so that you can still see the map at night
      AmbientLight ambientLight = new AmbientLight(
        Color.color(0.1, 0.1, 0.1)
      );
      getChildren().addAll(sun, moon, ambientLight);

      // Set the time to sunset just to update the lights to something sensible
      setTime(90);
      // Start a thread to update the game time on a regular interval
      new Thread(this::runDayNightCycle, "DayNightCycle").start();
    }

    // Zooming
    subScene.setOnScroll((e) -> {
      // Calculate a new zoom value from the current zoom and the scroll amount
      double newValue = camera.translate.getZ() + (e.getDeltaY() / 40);
      // Clamp the value to the min/max values
      if (newValue > -MAX_ZOOM) newValue = -MAX_ZOOM;
      if (newValue < -MIN_ZOOM) newValue = -MIN_ZOOM;
      // Set the new zoom value
      camera.translate.setZ(newValue);
    });

    // Panning
    subScene.setOnMouseDragged((e) -> {
      // Only drag the map if the left mouse button is pressed
      if (e.getButton() == MouseButton.PRIMARY) {
        // Get mouse position
        double x = e.getX(), y = e.getY();
        // Check if this is the first coordinate of the drag
        if (oldMouseX == -1 || oldMouseY == -1) {
          oldMouseX = x;
          oldMouseY = y;
        } else {
          // If it's not, work out how far we've dragged
          double dX = x - oldMouseX;
          double dY = y - oldMouseY;
          // ...and store the now old values
          oldMouseX = x;
          oldMouseY = y;

          // Multiply this movement by a value proportional to the zoom amount
          double multiplier = (Math.abs(camera.translate.getZ()) / 30) * 2;

          dX *= multiplier;
          dY *= multiplier;

          // Translate the camera by the dragged amount
          camera.translateBy(-dX / 250, -dY / 250, 0);
        }
      }
    });

    subScene.setOnMouseReleased((e) -> {
      switch (e.getButton()) {
        // If the left mouse button was released, reset the old drag
        // coordinates
        case PRIMARY:
          oldMouseX = -1;
          oldMouseY = -1;
          break;
        // If the right mouse button was released, reset pathfinding and move
        // units if they were selected
        case SECONDARY:
          this.root.resetPathfinding();
          break;
      }
    });
  }

  /**
   * Called by the client to register key handlers. These don't work with the
   * sub-scene.
   *
   * @param scene        scene to add key handlers to
   * @param eventHandler extra event handler for key events
   */
  public void setScene(
    Scene scene,
    EventHandler<? super KeyEvent> eventHandler
  ) {
    // Debug keyboard shortcuts
    scene.setOnKeyPressed((e) -> {
      switch (e.getCode()) {
        // Camera rotation for looking around the map
        case W:
          camera.rotateBy(-1, 0, 0);
          break;
        case S:
          camera.rotateBy(1, 0, 0);
          break;
        // Shortcut key to force rerender of all tiles
        case R:
          root.updateTileRenders();
          System.out.println("Updated tile renders");
          break;
      }
      // Handle extra shortcuts
      eventHandler.handle(e);
    });
  }

  /**
   * Function called in a separate thread to run the day/night cycle.
   */
  private void runDayNightCycle() {
    try {
      while (true) {
        // Work out the second of the current minute including fractional
        // component
        double second = (System.currentTimeMillis() / 1000.0) % 60.0;

        // Set the time on the UI thread as non-UI threads can't update UI
        // components
        Platform.runLater(() -> setTime(second * 6));

        // Try to update the time 30 times-per-second.
        Thread.sleep(1000 / 30);
      }
    } catch (InterruptedException ignored) {
    }
  }

  /**
   * Sets the time of day represented by the angle of the sun.
   *
   * @param angle angle of the sun: sunrise(0) - midday (90) - sunset(180)
   *              - midnight (270) - sunrise (360)
   */
  private void setTime(double angle) {
    // Update the angle of the sun/moon
    sunMoonRotate.setAngle(angle);

    // Calculate the intensity of the sun/moon
    double sunValue = Math.max(Math.sin(Math.toRadians(angle)), 0);
    double moonValue = (1 - sunValue) / 4;

    // Calculate/update  the colours of the sun/moon lights
    Color sunColour = Color.color(sunValue, sunValue, sunValue * 0.95);
    Color moonColour = Color.color(moonValue, moonValue, moonValue * 2);

    sun.setColor(sunColour);
    moon.setColor(moonColour);

    // Update the background colour of the game window
    subScene.setFill(sunColour);
  }
}
