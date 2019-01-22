package com.mrbbot.civilisation.ui;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

public class UIHelpers {
  public static Background colouredBackground(Color colour) {
    return new Background(new BackgroundFill(colour, null, null));
  }
}
