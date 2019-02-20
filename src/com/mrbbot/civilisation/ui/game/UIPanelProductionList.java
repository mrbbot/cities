package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.unit.UnitType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class UIPanelProductionList extends ScrollPane {
  UIPanelProductionList() {
    super();
    setPrefWidth(300);

    VBox list = new VBox();

    Label unitsTitle = new Label("Units");
    unitsTitle.getStyleClass().add("production-list-title");
    Label buildingsTitle = new Label("Buildings");
    buildingsTitle.getStyleClass().add("production-list-title");

    list.getChildren().add(unitsTitle);
    for (UnitType unit : UnitType.VALUES) list.getChildren().add(buildListItem(unit));
    list.getChildren().add(buildingsTitle);
    for (Building building : Building.VALUES) list.getChildren().add(buildListItem(building));

    setHbarPolicy(ScrollBarPolicy.NEVER);
    setVbarPolicy(ScrollBarPolicy.ALWAYS);

    setContent(list);
  }

  private Pane buildListItem(CityBuildable buildable) {
    BorderPane listItem = new BorderPane();
    listItem.getStyleClass().add("production-list-item");
    listItem.setMaxHeight(0);

    VBox list = new VBox();

    Label titleLabel = new Label(buildable.getName());
    titleLabel.setFont(new Font(16));
    Label descriptionLabel = new Label(buildable.getDescription());

    HBox detailsBox = new HBox(7);
    detailsBox.setPadding(new Insets(4, 0, 0, 0));
    for (CityBuildable.Detail detail : buildable.getDetails()) {
      detailsBox.getChildren().add(new Badge(detail.badge));
      if (!detail.text.isEmpty()) {
        detailsBox.getChildren().add(new Label(detail.text));
      }
    }

    list.getChildren().addAll(titleLabel, descriptionLabel, detailsBox);

    listItem.setCenter(list);
    listItem.setRight(new ProgressIndicator(0.5));

    return listItem;
  }
}
