package com.mrbbot.civilisation.ui;

import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.render.map.RenderMap;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class UIGame extends AnchorPane {
  private final RenderMap renderMap;

  public UIGame(RenderMap renderMap) {
    this.renderMap = renderMap;

    setPickOnBounds(false);

    //setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0.5), null, null)));

    /*Button newCityButton = new Button("Create City");
    AnchorPane.setBottomAnchor(newCityButton, 20.0);
    AnchorPane.setLeftAnchor(newCityButton, 20.0);
    newCityButton.setOnAction(e -> {
      renderMap.data.cities.add(new City(renderMap.data.hexagonGrid, 8, 8, Color.GREEN));
      renderMap.data.hexagonGrid.forEach((gridTile, _hex, _x, _y) -> gridTile.renderer.updateOverlay());
    });
    getChildren().add(newCityButton);*/

    UITechTree techTree = new UITechTree();
    setAnchors(techTree, 0, 0, 0, 0);
    getChildren().add(techTree);
  }

  private void setAnchors(Node node, int top, int left, int bottom, int right) {
    AnchorPane.setTopAnchor(node, (double) top);
    AnchorPane.setLeftAnchor(node, (double) left);
    AnchorPane.setBottomAnchor(node, (double) bottom);
    AnchorPane.setRightAnchor(node, (double) right);
  }
}
