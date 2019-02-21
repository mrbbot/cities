package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
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

@ClientOnly
public class UIPanelActions extends VBox implements EventHandler<ActionEvent> {
  private static final int ITEM_WIDTH = 200;
  private Unit selectedUnit;
  private Label selectedUnitLabel;
  private ComboBox<String> actionsComboBox;
  private ObservableList<String> actionsList;
  private Button actionButton, nextTurnButton;
  private BiConsumer<Unit, String> unitActionListener;

  UIPanelActions() {
    super(5);
    setPrefHeight(0);

    Label selectedUnitHeading = new Label("Selected unit:");
    selectedUnitLabel = new Label("None");
    selectedUnitLabel.setFont(new Font(24));
    selectedUnitLabel.setPadding(new Insets(0, 0, 5, 0));

    actionsList = FXCollections.observableArrayList();
    actionsComboBox = new ComboBox<>(actionsList);
    /*FXCollections.observableArrayList(
        "Farm",
        "Mine",
        "Road",
        "Railway"
      )*/
    actionsComboBox.setDisable(true);
    actionButton = new Button("");
    actionButton.setOnAction(this);
    actionButton.setDisable(true);

    nextTurnButton = new Button("Next Turn");
    //nextTurnButton.setDisable(true);
    nextTurnButton.setOnAction(this);
    nextTurnButton.setFont(new Font(20));

    actionsComboBox.setPrefWidth(ITEM_WIDTH);
    actionButton.setPrefWidth(ITEM_WIDTH);
    nextTurnButton.setPrefWidth(ITEM_WIDTH);

    getChildren().addAll(selectedUnitHeading, selectedUnitLabel, actionsComboBox, actionButton, nextTurnButton);
  }

  public void setUnitActionListener(BiConsumer<Unit, String> unitActionListener) {
    this.unitActionListener = unitActionListener;
  }

  void setSelectedUnit(Unit unit) {
    this.selectedUnit = unit;
    actionsList.clear();
    actionsComboBox.setValue("");
    actionsComboBox.setDisable(true);
    actionButton.setText("");
    actionButton.setDisable(true);
    if (unit == null) {
      selectedUnitLabel.setText("None");
    } else {
      Tile tile = unit.tile;

      selectedUnitLabel.setText(unit.unitType.getName());

      if (unit.unitType.hasAbility(UnitAbility.ABILITY_SETTLE)) {
        actionButton.setText("Settle");
        actionButton.setDisable(tile.city != null);
      }
    }
  }

  void setNextTurnWaiting(boolean waiting) {
    nextTurnButton.setDisable(waiting);
    nextTurnButton.setText(waiting ? "Waiting..." : "Next Turn");
  }

  @Override
  public void handle(ActionEvent event) {
    if(unitActionListener != null) {
      if(event.getSource() == actionButton) {
        unitActionListener.accept(selectedUnit, actionsComboBox.getValue());
      } else if(event.getSource() == nextTurnButton) {
        setNextTurnWaiting(true);
        Civilisation.CLIENT.broadcast(new PacketReady(true));
        unitActionListener.accept(null, null);
      }
    }
  }
}
