package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.PacketCityBuildRequest;
import com.mrbbot.civilisation.net.packet.PacketCityRename;
import com.mrbbot.civilisation.ui.UIHelpers;
import com.mrbbot.generic.net.ClientOnly;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ClientOnly
public class UIPanelCityDetails extends ScrollPane {
  private TextField cityNameField;
  private Button renameButton;
  private Label cityProductionLabel;
  private Label cityScienceLabel;
  private Label cityGoldLabel;
  private Label cityFoodLabel;
  private HashMap<CityBuildable, Pane> buildablePanes;
  private HashMap<CityBuildable, Tooltip> buildableTooltips;
  private HashMap<CityBuildable, ProgressIndicator> buildableProgresses;
  private ToggleGroup[] productionGoldToggleGroups;
  private RadioButton[] productionRadioButtons, goldRadioButtons;
  private boolean buildWithProduction;

  private Game lastSelectedGame;
  private City lastSelectedCity;
  private ArrayList<City> lastSelectedPlayersCities;

  UIPanelCityDetails() {
    super();
    setPrefWidth(300);

    buildablePanes = new HashMap<>();
    buildableTooltips = new HashMap<>();
    buildableProgresses = new HashMap<>();

    VBox list = new VBox();

    BorderPane detailsTitle = new BorderPane();
    detailsTitle.getStyleClass().add("production-list-title");
    detailsTitle.setCenter(cityNameField = new TextField());
    StackPane renamePane = new StackPane(renameButton = new Button("Rename"));
    renamePane.setPadding(new Insets(0, 0, 0, 5));
    detailsTitle.setRight(renamePane);

    HBox details = new HBox(7);
    details.setPadding(new Insets(10));
    details.getChildren().addAll(
      new Badge(BadgeType.PRODUCTION),
      cityProductionLabel = new Label("0"),
      new Badge(BadgeType.SCIENCE),
      cityScienceLabel = new Label("0"),
      new Badge(BadgeType.GOLD),
      cityGoldLabel = new Label("0"),
      new Badge(BadgeType.FOOD),
      cityFoodLabel = new Label("0 (0 citizens)")
    );
    list.getChildren().addAll(detailsTitle, details);

    productionGoldToggleGroups = new ToggleGroup[2];
    productionRadioButtons = new RadioButton[2];
    goldRadioButtons = new RadioButton[2];
    buildWithProduction = true;

    list.getChildren().add(buildListTitle("Units", 0));
    for (UnitType unit : UnitType.VALUES) list.getChildren().add(buildListItem(unit));
    list.getChildren().add(buildListTitle("Buildings", 1));
    for (Building building : Building.VALUES) list.getChildren().add(buildListItem(building));

    setHbarPolicy(ScrollBarPolicy.NEVER);
    setVbarPolicy(ScrollBarPolicy.ALWAYS);

    setContent(list);
  }

  @SuppressWarnings("Duplicates")
  private Pane buildListTitle(String title, int i) {
    BorderPane pane = new BorderPane();
    pane.getStyleClass().add("production-list-title");

    pane.setLeft(new Label(title));

    final ToggleGroup toggleGroup = new ToggleGroup();
    productionGoldToggleGroups[i] = toggleGroup;

    HBox prodGoldBox = new HBox();
    Label buildWithLabel = new Label("Build with");

    final RadioButton productionButton = new RadioButton();
    productionRadioButtons[i] = productionButton;
    productionButton.setToggleGroup(toggleGroup);
    productionButton.setSelected(true);
    StackPane productionButtonPane = new StackPane(productionButton);
    productionButtonPane.setPadding(new Insets(0, 0, 0, 7));

    final RadioButton goldButton = new RadioButton();
    goldRadioButtons[i] = goldButton;
    goldButton.setToggleGroup(toggleGroup);
    StackPane goldButtonPane = new StackPane(goldButton);
    goldButtonPane.setPadding(new Insets(0, 0, 0, 7));

    toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
      boolean newBuildWithProduction = newValue == productionButton;
      if (buildWithProduction != newBuildWithProduction) {
        buildWithProduction = newBuildWithProduction;
        int otherIndex = (i + 1) % 2;
        productionGoldToggleGroups[otherIndex].selectToggle(
          buildWithProduction
            ? productionRadioButtons[otherIndex]
            : goldRadioButtons[otherIndex]
        );
        setSelectedCity(
          lastSelectedGame,
          lastSelectedCity,
          lastSelectedPlayersCities
        );
      }
    });

    prodGoldBox.getChildren().addAll(
      buildWithLabel,
      productionButtonPane,
      new Badge(BadgeType.PRODUCTION),
      goldButtonPane,
      new Badge(BadgeType.GOLD)
    );
    pane.setRight(prodGoldBox);

    return pane;
  }

  private Pane buildListItem(CityBuildable buildable) {
    BorderPane listItem = new BorderPane();
    listItem.getStyleClass().add("production-list-item");
    listItem.setMaxHeight(0);
    Tooltip tooltip = new Tooltip("Click to build this in the city");
    buildableTooltips.put(buildable, tooltip);
    Tooltip.install(listItem, tooltip);

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
    ProgressIndicator progressIndicator = new ProgressIndicator(0.0);
    progressIndicator.setVisible(false);
    buildableProgresses.put(buildable, progressIndicator);
    listItem.setRight(progressIndicator);

    buildablePanes.put(buildable, listItem);

    return listItem;
  }

  void setSelectedCity(Game game, City city, ArrayList<City> playersCities) {
    lastSelectedGame = game;
    lastSelectedCity = city;
    lastSelectedPlayersCities = playersCities;

    cityProductionLabel.setText(String.valueOf(city.getProductionPerTurn()));
    cityScienceLabel.setText(String.valueOf(city.getSciencePerTurn()));
    cityGoldLabel.setText(String.valueOf(city.getGoldPerTurn()));
    cityFoodLabel.setText(String.format(
      "%d (%d citizen%s)",
      city.getFoodPerTurn(),
      city.citizens,
      city.citizens == 1 ? "" : "s"
    ));
    cityNameField.setText(city.name);
    renameButton.setOnAction((e) -> {
      String newName = cityNameField.getText();
      city.name = newName;
      Civilisation.CLIENT.broadcast(new PacketCityRename(city.getX(), city.getY(), newName));
    });

    for (Map.Entry<CityBuildable, Pane> buildableEntry : buildablePanes.entrySet()) {
      CityBuildable buildable = buildableEntry.getKey();
      Pane buildablePane = buildableEntry.getValue();
      Tooltip buildableTooltip = buildableTooltips.get(buildable);
      ProgressIndicator progressIndicator = buildableProgresses.get(buildable);

      boolean currentlyBuildingThis = buildable.equals(city.currentlyBuilding);
      String cantBuildReason = buildable.canBuildGivenCities(city, playersCities);

      String tooltipText = "Click to build this in the city";
      boolean canBuild = true;
      if (currentlyBuildingThis) {
        tooltipText = "You are currently building this";
        canBuild = false;
      } else if (city.currentlyBuilding != null) {
        tooltipText = "You are currently building something else";
        canBuild = false;
      } else if (!buildWithProduction && game.getPlayerGoldTotal(city.player.id) < buildable.getGoldCost()) {
        tooltipText = "You don't have enough gold to build this";
        canBuild = false;
      } else if (cantBuildReason != null && cantBuildReason.length() > 0) {
        tooltipText = cantBuildReason;
        canBuild = false;
      }

      buildableTooltip.setText(tooltipText);

      UIHelpers.toggleClass(buildablePane, "can-build", canBuild);
      UIHelpers.toggleClass(buildablePane, "building", currentlyBuildingThis);

      if (currentlyBuildingThis) {
        progressIndicator.setProgress(
          Math.min((double) city.productionTotal / (double) buildable.getProductionCost(), 1.0)
        );
      }
      progressIndicator.setVisible(currentlyBuildingThis);

      if (canBuild) {
        buildablePane.setOnMouseClicked(e -> {
          PacketCityBuildRequest packetCityBuildRequest = new PacketCityBuildRequest(
            city.getX(),
            city.getY(),
            buildable,
            buildWithProduction
          );
          game.handlePacket(packetCityBuildRequest);
          setSelectedCity(game, city, playersCities);
          Civilisation.CLIENT.broadcast(packetCityBuildRequest);
          System.out.println("Clicked on " + buildable.getName());
        });
      } else {
        buildablePane.setOnMouseClicked(null);
      }
    }
  }
}
