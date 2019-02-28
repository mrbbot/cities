package com.mrbbot.civilisation.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Abstract class representing a screen that can be displayed. Currently there
 * are only two screens, the connection screen and the game screen. The screen
 * should be able to create a JavaFX scene containing the required UI
 * components.
 */
public abstract class Screen {
  /**
   * Creates a scene representing this screen
   *
   * @param stage  stage the scene would be placed in
   * @param width  width of the screen
   * @param height height of the screen
   * @return scene representing this screen
   */
  public abstract Scene makeScene(Stage stage, int width, int height);
}
