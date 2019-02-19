package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.unit.UnitType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;

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
    for (UnitType unit : UnitType.values()) {
      ArrayList<Detail> details = new ArrayList<>();

      details.add(new Detail(new Badge(unit.color, unit.name.charAt(0)), ""));
      details.add(new Detail(Badge.makeProductionBadge(), String.valueOf(unit.productionCost)));

      list.getChildren().add(buildListItem(unit.name, unit.description, details));
    }

    list.getChildren().add(buildingsTitle);
    for (Building building : Building.values()) {
      ArrayList<Detail> details = new ArrayList<>();

      details.add(new Detail(Badge.makeProductionBadge(), String.valueOf(building.productionCost)));

      list.getChildren().add(buildListItem(building.name, building.description, details));
    }

    setHbarPolicy(ScrollBarPolicy.NEVER);
    setVbarPolicy(ScrollBarPolicy.ALWAYS);

    setContent(list);
  }

  private class Detail {
    private final Badge badge;
    private final String text;

    private Detail(Badge badge, String text) {
      this.badge = badge;
      this.text = text;
    }
  }

  private VBox buildListItem(String title, String description, ArrayList<Detail> details) {
    VBox listItem = new VBox();
    listItem.getStyleClass().add("production-list-item");
    listItem.setMaxHeight(0);

    Label titleLabel = new Label(title);
    titleLabel.setFont(new Font(16));
    Label descriptionLabel = new Label(description);

    HBox detailsBox = new HBox(7);
    detailsBox.setPadding(new Insets(4, 0, 0, 0));
    for (Detail detail : details) {
      detailsBox.getChildren().add(detail.badge);
      if (!detail.text.isEmpty()) {
        detailsBox.getChildren().add(new Label(detail.text));
      }
    }

    listItem.getChildren().addAll(titleLabel, descriptionLabel, detailsBox);
    return listItem;
  }
}
