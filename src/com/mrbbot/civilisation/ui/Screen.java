package com.mrbbot.civilisation.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class Screen {
  public abstract Scene makeScene(Stage stage, int width, int height);
}
