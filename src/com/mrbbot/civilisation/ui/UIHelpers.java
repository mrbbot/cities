package com.mrbbot.civilisation.ui;

import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

public class UIHelpers {
  public static Background colouredBackground(Color colour) {
    return new Background(new BackgroundFill(colour, null, null));
  }

  public static void toggleClass(Node node, String className, boolean value) {
    if(value) {
      if(!node.getStyleClass().contains(className)) {
        node.getStyleClass().add(className);
      }
    } else {
      node.getStyleClass().remove(className);
    }
  }
}
