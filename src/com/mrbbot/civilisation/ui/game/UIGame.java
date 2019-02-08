package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.net.packet.PacketChat;
import com.mrbbot.civilisation.net.packet.PacketCityCreate;
import com.mrbbot.civilisation.render.map.RenderMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class UIGame extends AnchorPane {
  private static final Insets PANEL_PADDING = new Insets(10);

  private final RenderMap renderMap;
  private final Color playerColor;

  private final UIPanelTech panelTech;
  private final UIPanelChat panelChat;
  private final UIPanelActions panelActions;
  private final UITechTree techTree;
  private final Button closeTechTreeButton;

  public UIGame(RenderMap renderMap, int width, int height) {
    this.renderMap = renderMap;
    setPickOnBounds(false);

    playerColor = this.renderMap.currentPlayer.getColour();

    panelTech = new UIPanelTech();
    panelTech.setBorder(makePanelBorder(Pos.BOTTOM_RIGHT));
    panelTech.setBackground(makePanelBackground(Pos.BOTTOM_RIGHT));
    panelTech.setPadding(PANEL_PADDING);
    panelTech.setOnOpenTechTree(e -> setTechTreeVisible(true));
    AnchorPane.setTopAnchor(panelTech, 0.0);
    AnchorPane.setLeftAnchor(panelTech, 0.0);

    panelChat = new UIPanelChat(renderMap.currentPlayer.id);
    panelChat.setBorder(makePanelBorder(Pos.BOTTOM_LEFT));
    panelChat.setBackground(makePanelBackground(Pos.BOTTOM_LEFT));
    panelChat.setPadding(PANEL_PADDING);
    AnchorPane.setTopAnchor(panelChat, 0.0);
    AnchorPane.setRightAnchor(panelChat, 0.0);

    panelActions = new UIPanelActions();
    panelActions.setBorder(makePanelBorder(Pos.TOP_LEFT));
    panelActions.setBackground(makePanelBackground(Pos.TOP_LEFT));
    panelActions.setPadding(PANEL_PADDING);
    panelActions.setUnitActionListener(this::onUnitAction);
    AnchorPane.setBottomAnchor(panelActions, 0.0);
    AnchorPane.setRightAnchor(panelActions, 0.0);

    getChildren().addAll(panelTech, panelChat, panelActions);

    //setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0.5), null, null)));

    /*Button newCityButton = new Button("Create City");
    AnchorPane.setBottomAnchor(newCityButton, 20.0);
    AnchorPane.setLeftAnchor(newCityButton, 20.0);
    newCityButton.setOnAction(e -> {
      renderMap.data.cities.add(new City(renderMap.data.hexagonGrid, 8, 8, Color.GREEN));
      renderMap.data.hexagonGrid.forEach((gridTile, _hex, _x, _y) -> gridTile.renderer.updateRender());
    });
    getChildren().add(newCityButton);*/

    techTree = new UITechTree(height);
    setAnchors(techTree, 0, 0, 0, 0);
    closeTechTreeButton = new Button("Close Tech Tree");
    closeTechTreeButton.setOnAction(e -> setTechTreeVisible(false));
    AnchorPane.setTopAnchor(closeTechTreeButton, 20.0);
    AnchorPane.setLeftAnchor(closeTechTreeButton, 20.0);
    //getChildren().add(techTree);
  }

  private void setAnchors(Node node, int top, int left, int bottom, int right) {
    AnchorPane.setTopAnchor(node, (double) top);
    AnchorPane.setLeftAnchor(node, (double) left);
    AnchorPane.setBottomAnchor(node, (double) bottom);
    AnchorPane.setRightAnchor(node, (double) right);
  }

  private CornerRadii makeCornerRadiiForCutout(Pos cutout, int size) {
    return new CornerRadii(
      cutout == Pos.TOP_LEFT ? size : 0,
      cutout == Pos.TOP_RIGHT ? size : 0,
      cutout == Pos.BOTTOM_RIGHT ? size : 0,
      cutout == Pos.BOTTOM_LEFT ? size : 0,
      false
    );
  }

  private Border makePanelBorder(Pos cutout) {
    return new Border(new BorderStroke(
      playerColor,
      BorderStrokeStyle.SOLID,
      makeCornerRadiiForCutout(cutout, 10),
      new BorderWidths(10)
    ));
  }

  private Background makePanelBackground(Pos cutout) {
    return new Background(new BackgroundFill(
      Color.WHITE,
      makeCornerRadiiForCutout(cutout, 20),
      null
    ));
  }

  void onSelectedUnitChanged(Unit unit) {
    panelActions.setSelectedUnit(unit);
  }

  private void onUnitAction(Unit unit, String actionDetails) {
    if(unit == null) {
      System.out.println("Next turn...");
    } else {
      System.out.println((unit.unitType.name + " performed an action (details: \"" + actionDetails + "\")"));
      switch (unit.unitType) {
        case SETTLER:
          Civilisation.CLIENT.broadcast(new PacketCityCreate(renderMap.currentPlayer.id, unit.tile.x, unit.tile.y));
          renderMap.data.cities.add(new City(renderMap.data.hexagonGrid, unit.tile.x, unit.tile.y, renderMap.currentPlayer));
          renderMap.updateTileRenders();
          renderMap.setSelectedUnit(null);
          renderMap.deleteUnit(unit);
          break;
        case SCOUT:
          break;
        case WARRIOR:
          break;
        case ARCHER:
          break;
        case WORKER:
          break;
        case ROCKET:
          break;
      }
    }
  }

  public void setTechTreeVisible(boolean visible) {
    if(visible) {
      getChildren().add(techTree);
      getChildren().add(closeTechTreeButton);
    } else {
      getChildren().remove(techTree);
      getChildren().remove(closeTechTreeButton);
    }
  }

  public void handlePacketChat(PacketChat packet) {
    panelChat.addMessage(packet.message);
  }
}
