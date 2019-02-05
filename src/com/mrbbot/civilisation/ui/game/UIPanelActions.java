package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.unit.Unit;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class UIPanelActions extends VBox {
  private static final int ITEM_WIDTH = 200;
  private Unit selectedUnit;

  UIPanelActions() {
    super(5);
    setPrefHeight(0);

    Label selectedUnitHeading = new Label("Selected unit:");
    Label selectedUnit = new Label("None");
    selectedUnit.setFont(new Font(24));
    selectedUnit.setPadding(new Insets(0, 0, 5, 0));

    ComboBox<String> actionsComboBox = new ComboBox<>(
      FXCollections.observableArrayList(
        /*"Farm",
        "Mine",
        "Road",
        "Railway"*/
      )
    );
    actionsComboBox.setDisable(true);
    Button actionButton = new Button("Action");
    actionButton.setDisable(true);

    Button nextTurnButton = new Button("Next Turn");
    nextTurnButton.setDisable(true);
    nextTurnButton.setFont(new Font(20));

    actionsComboBox.setPrefWidth(ITEM_WIDTH);
    actionButton.setPrefWidth(ITEM_WIDTH);
    nextTurnButton.setPrefWidth(ITEM_WIDTH);

    getChildren().addAll(selectedUnitHeading, selectedUnit, actionsComboBox, actionButton, nextTurnButton);
  }

  public void setSelectedUnit(Unit unit) {
    this.selectedUnit = unit;
  }
}
