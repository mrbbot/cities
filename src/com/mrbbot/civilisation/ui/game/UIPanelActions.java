package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.PacketReady;
import com.mrbbot.generic.net.ClientOnly;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.function.BiConsumer;

/**
 * UI panel for controlling the selected unit's actions and for declaring the
 * current player ready for the next turn. Extends {@link VBox} so that items
 * are arranged in a column.
 */
@ClientOnly
public class UIPanelActions extends VBox implements EventHandler<ActionEvent> {
  /**
   * Constant for the preferred width of items in the action list
   */
  private static final int ITEM_WIDTH = 200;
  /**
   * Currently selected unit by the player
   */
  private Unit selectedUnit;
  /**
   * Unit for describing the type of the selected unit
   */
  private Label selectedUnitLabel;
  /**
   * Combo box for the different types of actions that can be performed (only
   * used for worker improvement types at the moment)
   */
  private ComboBox<String> actionsComboBox;
  /**
   * List of the available actions for the actions box
   */
  private ObservableList<String> actionsList;
  /**
   * Button to perform the selected action with the selected unit
   */
  private Button actionButton;
  /**
   * Button to mark the current player as ready and wait for other players to
   * complete their turn
   */
  private Button nextTurnButton;
  /**
   * Callback function for any actions to be performed. The first parameter is
   * the selected unit or null if the next turn button is pressed. The second
   * parameter contains information of the details of the action (i.e type of
   * improvement)
   */
  private BiConsumer<Unit, String> unitActionListener;

  UIPanelActions() {
    // Set vertical height
    super(5);
    // Make this panel occupy the minimum height
    setPrefHeight(0);

    // Create the selected unit label and heading
    Label selectedUnitHeading = new Label("Selected unit:");
    selectedUnitLabel = new Label("None");
    selectedUnitLabel.setFont(new Font(24));
    selectedUnitLabel.setPadding(
      new Insets(0, 0, 5, 0)
    );

    // Create the actions combo box and button
    actionsList = FXCollections.observableArrayList();
    actionsComboBox = new ComboBox<>(actionsList);
    actionsComboBox.setDisable(true);
    actionButton = new Button("");
    // Register this as the click handler
    actionButton.setOnAction(this);
    // Disable it by default
    actionButton.setDisable(true);

    // Create the next turn button
    nextTurnButton = new Button("Next Turn");
    // Register this as the click handler
    nextTurnButton.setOnAction(this);
    // Make the text a bit bigger than usual
    nextTurnButton.setFont(new Font(20));

    actionsComboBox.setPrefWidth(ITEM_WIDTH);
    actionButton.setPrefWidth(ITEM_WIDTH);
    nextTurnButton.setPrefWidth(ITEM_WIDTH);

    // Add all the components to the vertical stack
    getChildren().addAll(
      selectedUnitHeading,
      selectedUnitLabel,
      actionsComboBox,
      actionButton,
      nextTurnButton
    );
  }

  /**
   * Sets the action listener for performing unit actions and requesting the
   * next turn.
   *
   * @param unitActionListener new unit action listener
   */
  public void setUnitActionListener(
    BiConsumer<Unit, String> unitActionListener
  ) {
    this.unitActionListener = unitActionListener;
  }

  /**
   * Sets the current selected unit. Called when the player selects a new unit.
   *
   * @param game game containing the unit
   * @param unit new selected unit or null if no unit is selected
   */
  void setSelectedUnit(Game game, Unit unit) {
    this.selectedUnit = unit;

    // Reset the UI
    actionsList.clear();
    actionsComboBox.setValue("");
    actionsComboBox.setDisable(true);
    actionButton.setText("");
    actionButton.setDisable(true);
    if (unit == null) {
      // If there's no unit selected, use none as the type
      selectedUnitLabel.setText("None");
    } else {
      Tile tile = unit.tile;

      // Set the unit label's text to be the type of the unit
      selectedUnitLabel.setText(unit.unitType.getName());

      // Depending on the units abilities enable different UI components

      // If the unit can settle...
      if (unit.hasAbility(UnitAbility.ABILITY_SETTLE)) {
        // Set the action
        actionButton.setText("Settle");
        // Enable the button if there isn't already a city on the tile
        actionButton.setDisable(tile.city != null);
      }

      // If the unit can improve...
      if (unit.hasAbility(UnitAbility.ABILITY_IMPROVE)) {
        // Add all the improvements that a worker can always do
        for (Improvement improvement : Improvement.VALUES) {
          // Check the player has unlocked the improvement
          if (improvement.workerCanDo
            && game.playerHasUnlocked(unit.player.id, improvement)) {
            actionsList.add(improvement.name);
          }
        }
        // Add chop forest if there's a tree and the player has unlocked it
        if (tile.improvement == Improvement.TREE
          && game.playerHasUnlocked(unit.player.id, Improvement.CHOP_FOREST)) {
          actionsList.add(Improvement.CHOP_FOREST.name);
        }

        // Set the action
        actionButton.setText("Improve");

        // Set the default improvement
        if (actionsList.size() > 0) {
          actionsComboBox.setValue(actionsList.get(0));
          boolean canImprove = tile.city == null
            || !tile.city.player.equals(unit.player);
          actionsComboBox.setDisable(canImprove);
          actionButton.setDisable(canImprove);
        }

        // If the unit is already building something disable the button and
        // show the progress of the build
        if (unit.workerBuilding != Improvement.NONE) {
          actionButton.setText(
            String.format(
              "Improving... (%d turns remaining)",
              unit.workerBuildTurnsRemaining
            )
          );
          actionsComboBox.setValue(unit.workerBuilding.name);
          actionsComboBox.setDisable(true);
          actionButton.setDisable(true);
        }
      }

      // If the unit can be upgraded and the player has unlocked the upgraded
      // type...
      UnitType upgradedType = unit.unitType.getUpgrade();
      if (upgradedType != null
        && game.playerHasUnlocked(unit.player.id, upgradedType)) {
        // Set the action
        actionButton.setText("Upgrade to " + upgradedType.getName());
        actionButton.setDisable(false);
      }

      // If the unit can blast off, set the action
      if (unit.hasAbility(UnitAbility.ABILITY_BLAST_OFF)) {
        actionButton.setText("Blast off!");
        actionButton.setDisable(false);
      }
    }
  }

  /**
   * Sets whether or not the game is waiting for players
   *
   * @param waiting whether the game is waiting for other players
   */
  void setNextTurnWaiting(boolean waiting) {
    // Disable the button if we're waiting
    nextTurnButton.setDisable(waiting);
    // Set the button text according to the current state
    nextTurnButton.setText(waiting ? "Waiting..." : "Next Turn");
  }

  /**
   * Handle the next turn/action button events
   *
   * @param event JavaFX event for the button click containing information on
   *              the source of the event
   */
  @Override
  public void handle(ActionEvent event) {
    if (unitActionListener != null) {
      if (event.getSource() == actionButton) {
        // If this was the action button, send the action's event with details
        unitActionListener.accept(selectedUnit, actionsComboBox.getValue());
      } else if (event.getSource() == nextTurnButton) {
        // Otherwise, it was the next turn button, so declare the player ready
        // and broadcast this
        setNextTurnWaiting(true);
        Civilisation.CLIENT.broadcast(new PacketReady(true));
        unitActionListener.accept(null, null);
      }
    }
  }
}
