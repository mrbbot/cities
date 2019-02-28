package com.mrbbot.civilisation.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

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
