package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.render.map.RenderMap;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class UIGame extends AnchorPane {
  private final RenderMap renderMap;
  private final Color playerColor;

  public UIGame(RenderMap renderMap, int width, int height) {
    this.renderMap = renderMap;
    setPickOnBounds(false);

    playerColor = this.renderMap.currentPlayer.getColour();

    Border panelBorder = new Border(new BorderStroke(
      playerColor,
      BorderStrokeStyle.SOLID,
      new CornerRadii(0, 0, 10, 0, false),
      new BorderWidths(10)
    ));

    UIPanelTech panelTech = new UIPanelTech(panelBorder);
    AnchorPane.setTopAnchor(panelTech, 0.0);
    AnchorPane.setLeftAnchor(panelTech, 0.0);
    getChildren().add(panelTech);

    //setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0.5), null, null)));

    /*Button newCityButton = new Button("Create City");
    AnchorPane.setBottomAnchor(newCityButton, 20.0);
    AnchorPane.setLeftAnchor(newCityButton, 20.0);
    newCityButton.setOnAction(e -> {
      renderMap.data.cities.add(new City(renderMap.data.hexagonGrid, 8, 8, Color.GREEN));
      renderMap.data.hexagonGrid.forEach((gridTile, _hex, _x, _y) -> gridTile.renderer.updateRender());
    });
    getChildren().add(newCityButton);*/

    UITechTree techTree = new UITechTree(height);
    setAnchors(techTree, 0, 0, 0, 0);
    //getChildren().add(techTree);
  }

  private void setAnchors(Node node, int top, int left, int bottom, int right) {
    AnchorPane.setTopAnchor(node, (double) top);
    AnchorPane.setLeftAnchor(node, (double) left);
    AnchorPane.setBottomAnchor(node, (double) bottom);
    AnchorPane.setRightAnchor(node, (double) right);
  }
}
