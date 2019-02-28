package com.mrbbot.civilisation.ui;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

/**
 * Utility class containing common functions used by the UI package.
 */
public class UIHelpers {
  /**
   * Creates a solid background of a single colour
   *
   * @param colour colour to make the background
   * @return background object filled with the specified colour
   */
  public static Background colouredBackground(Color colour) {
    return new Background(new BackgroundFill(colour, null, null));
  }

  /**
   * Forces the specified CSS class to either be enabled or disabled
   *
   * @param node      node to toggle the class of
   * @param className CSS class to be toggled
   * @param active    whether to enable the class
   */
  public static void toggleClass(Node node, String className, boolean active) {
    if (active) {
      // Add the class if it's not already there
      if (!node.getStyleClass().contains(className)) {
        node.getStyleClass().add(className);
      }
    } else {
      // Otherwise remove it
      node.getStyleClass().remove(className);
    }
  }

  /**
   * Show a modal dialog to the user
   *
   * @param message message to show
   * @param isError whether to show an error or information icon
   */
  public static void showDialog(String message, boolean isError) {
    // Create the dialog
    Alert dialog = new Alert(
      isError
        ? Alert.AlertType.ERROR
        : Alert.AlertType.INFORMATION
    );
    dialog.setTitle(isError ? "Error" : "Message");
    dialog.setContentText(message);
    // Show the dialog
    dialog.show();
  }
}
