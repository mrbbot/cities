package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.techs.PlayerTechDetails;
import com.mrbbot.generic.net.ClientOnly;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

/**
 * UI panel for basic tech details (what the player is currently researching
 * and their progress towards unlocking it)
 */
@ClientOnly
public class UIPanelTech extends BorderPane {
  /**
   * Label containing the name of the player's current research or "Nothing" if
   * they aren't researching anything
   */
  private Label currentlyResearching;
  /**
   * Progress towards unlocking the current technology
   */
  private ProgressIndicator progress;
  /**
   * Button that when clicked should show the tech tree UI
   */
  private Button openTechTree;

  UIPanelTech() {
    super();

    // Create the current researching label
    Label currentlyResearchingHeading =
      new Label("Currently researching:");
    currentlyResearching = new Label("Nothing");
    currentlyResearching.setFont(new Font(24));
    currentlyResearching.setPadding(
      new Insets(0, 0, 5, 0)
    );

    // Create the unlock progress indicator
    progress = new ProgressIndicator(0.5);
    progress.setPadding(new Insets(5, 5, 5, 0));

    // Create the open button
    openTechTree = new Button("Open Tech Tree");
    openTechTree.setPrefWidth(230);

    // Position the elements in the border pane
    setTop(currentlyResearchingHeading);
    setLeft(progress);
    setCenter(currentlyResearching);
    setBottom(openTechTree);
  }

  /**
   * Update the UI to reflect new player tech details. Called when a player
   * chooses a new technology to research or the progress of the current
   * project is updated (on new turn)
   *
   * @param details new tech details object for the player
   */
  void setTechDetails(PlayerTechDetails details) {
    // Set the currently researching text to the name of the current tech or
    // "Nothing" if no tech is being researched.
    this.currentlyResearching.setText(details.currentlyUnlocking == null
      ? "Nothing"
      : details.currentlyUnlocking.getName()
    );
    // Set the progress indicator to reflect the current percent unlocked
    this.progress.setProgress(details.percentUnlocked);
  }

  /**
   * Sets the listener to be called when the open tech tree button is pressed.
   * Should show the tech tree UI.
   *
   * @param value listener to be called when the button is pressed
   */
  void setOnOpenTechTree(EventHandler<ActionEvent> value) {
    openTechTree.setOnAction(value);
  }
}
