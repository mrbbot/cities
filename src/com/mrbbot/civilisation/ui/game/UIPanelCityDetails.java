package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.PacketCityBuildRequest;
import com.mrbbot.civilisation.net.packet.PacketCityRename;
import com.mrbbot.civilisation.render.map.RenderGame;
import com.mrbbot.civilisation.ui.UIHelpers;
import com.mrbbot.generic.net.ClientOnly;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * UI panel for showing a cities details and the production list where new
 * units and buildings can be built in the city.
 */
@ClientOnly
public class UIPanelCityDetails extends ScrollPane {
  /**
   * Game the selected city will be contained in
   */
  private final RenderGame renderGame;
  /**
   * Text field for the name of the city
   */
  private TextField cityNameField;
  /**
   * Button that when clicked will update the name of the city to reflect the
   * name field.
   */
  private Button renameButton;
  /**
   * Label for the city's current production
   */
  private Label cityProductionLabel;
  /**
   * Label for the city's current science
   */
  private Label cityScienceLabel;
  /**
   * Label for the city's current gold
   */
  private Label cityGoldLabel;
  /**
   * Label for the city's current food
   */
  private Label cityFoodLabel;
  /**
   * Panes that contain details on the mapped city buildables in the production
   * list
   */
  private HashMap<CityBuildable, Pane> buildablePanes;
  /**
   * Tooltips for the mapped city buildables in the production list
   */
  private HashMap<CityBuildable, Tooltip> buildableTooltips;
  /**
   * Progress indicators for the mapped city buildables in the production list
   */
  private HashMap<CityBuildable, ProgressIndicator> buildableProgresses;
  /**
   * Array containing the 2 toggle groups for the gold/production buttons.
   * These should be kept in sync with each other.
   */
  private ToggleGroup[] productionGoldToggleGroups;
  /**
   * Array containing the 2 "build with production" radio buttons. These should
   * have their selected state kept in sync with each other.
   */
  private RadioButton[] productionRadioButtons;
  /**
   * Array containing the 2 "build with gold" radio buttons. These should have
   * their selected state kept in sync with each other.
   */
  private RadioButton[] goldRadioButtons;
  /**
   * Whether or not new buildings should be built with production or gold.
   */
  private boolean buildWithProduction;

  /**
   * The game containing the last selected city
   */
  private Game lastSelectedGame;
  /**
   * The city that was last selected by the player
   */
  private City lastSelectedCity;
  /**
   * All the cities that belonged to the owner of the last selected city
   */
  private ArrayList<City> lastSelectedPlayersCities;

  UIPanelCityDetails(RenderGame renderGame) {
    super();
    this.renderGame = renderGame;
    setPrefWidth(300);

    // Create empty maps for buildable UI components
    buildablePanes = new HashMap<>();
    buildableTooltips = new HashMap<>();
    buildableProgresses = new HashMap<>();

    VBox list = new VBox();

    // Create the details pane for showing information (production, gold, etc)
    // on a city
    BorderPane detailsTitle = new BorderPane();
    detailsTitle.getStyleClass().add("production-list-title");

    // Create the rename field and button
    detailsTitle.setCenter(cityNameField = new TextField());
    StackPane renamePane =
      new StackPane(renameButton = new Button("Rename"));
    renamePane.setPadding(new Insets(0, 0, 0, 5));
    detailsTitle.setRight(renamePane);

    // Create the details row
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

    // Create arrays for production/gold buttons
    productionGoldToggleGroups = new ToggleGroup[2];
    productionRadioButtons = new RadioButton[2];
    goldRadioButtons = new RadioButton[2];
    // By default, build buildables with production
    buildWithProduction = true;

    // Add the units header
    list.getChildren().add(buildListTitle("Units", 0));
    for (UnitType unit : UnitType.VALUES) {
      // Add all buildable unit types
      list.getChildren().add(buildListItem(unit));
    }
    // Add the buildings header
    list.getChildren().add(buildListTitle("Buildings", 1));
    for (Building building : Building.VALUES) {
      // Add all buildable buildings
      list.getChildren().add(buildListItem(building));
    }

    // Always show the vertical scrollbar
    setHbarPolicy(ScrollBarPolicy.NEVER);
    setVbarPolicy(ScrollBarPolicy.ALWAYS);

    setContent(list);
  }

  /**
   * Creates a heading for a buildable list section (units/buildings) with
   * radio buttons for choosing between building with gold and production
   *
   * @param title title for the heading text
   * @param i     index of the section
   * @return pane containing the title
   */
  @SuppressWarnings("Duplicates")
  private Pane buildListTitle(String title, int i) {
    BorderPane pane = new BorderPane();
    // Add a CSS class for styling
    pane.getStyleClass().add("production-list-title");

    // Add the title
    pane.setLeft(new Label(title));

    //Create a toggle group for this set of buttons
    final ToggleGroup toggleGroup = new ToggleGroup();
    productionGoldToggleGroups[i] = toggleGroup;

    HBox prodGoldBox = new HBox();
    Label buildWithLabel = new Label("Build with");

    // Create build with production and gold buttons
    final RadioButton productionButton = new RadioButton();
    productionRadioButtons[i] = productionButton;
    productionButton.setToggleGroup(toggleGroup);
    productionButton.setSelected(true);
    StackPane productionButtonPane = new StackPane(productionButton);
    productionButtonPane.setPadding(
      new Insets(0, 0, 0, 7)
    );

    final RadioButton goldButton = new RadioButton();
    goldRadioButtons[i] = goldButton;
    goldButton.setToggleGroup(toggleGroup);
    StackPane goldButtonPane = new StackPane(goldButton);
    goldButtonPane.setPadding(new Insets(0, 0, 0, 7));

    // Keep the buttons in this header in sync with the other header by
    // watching for changes
    toggleGroup.selectedToggleProperty().addListener(
      (observable, oldValue, newValue) -> {
        // Whether we're now building with production
        boolean newBuildWithProduction = newValue == productionButton;
        if (buildWithProduction != newBuildWithProduction) {
          // If they're different, update the other button
          buildWithProduction = newBuildWithProduction;
          int otherIndex = (i + 1) % 2;
          productionGoldToggleGroups[otherIndex].selectToggle(
            buildWithProduction
              ? productionRadioButtons[otherIndex]
              : goldRadioButtons[otherIndex]
          );
          // Recalculate whether or not each item can be built/purchased
          setSelectedCity(
            lastSelectedGame,
            lastSelectedCity,
            lastSelectedPlayersCities
          );
        }
      }
    );

    // Add build with UI components
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

  /**
   * Build a list item for a {@link CityBuildable}. Clicking on this will build
   * it in the selected city.
   *
   * @param buildable buildable to constructor a list item for
   * @return list item for that buildable
   */
  private Pane buildListItem(CityBuildable buildable) {
    BorderPane listItem = new BorderPane();
    // Add CSS class form styling
    listItem.getStyleClass().add("production-list-item");
    // Make it fill as little height as possible
    listItem.setMaxHeight(0);

    // Create a tooltip that shows up on hover
    Tooltip tooltip = new Tooltip("Click to build this in the city");
    buildableTooltips.put(buildable, tooltip);
    Tooltip.install(listItem, tooltip);

    VBox list = new VBox();

    // Add the title and description of the buildable
    Label titleLabel = new Label(buildable.getName());
    titleLabel.setFont(new Font(16));
    Label descriptionLabel = new Label(buildable.getDescription());

    // Add the details of the buildable in a row with the relevant badges
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

    // Add a progress indicator for the build progress
    ProgressIndicator progressIndicator = new ProgressIndicator(0.0);
    progressIndicator.setVisible(false);
    buildableProgresses.put(buildable, progressIndicator);
    listItem.setRight(progressIndicator);

    // Store the pane later so it can be made translucent/invisible
    buildablePanes.put(buildable, listItem);

    return listItem;
  }

  /**
   * Sets the currently selected city and updates the UI to reflect what can be
   * built there.
   *
   * @param game          game the city is contained in
   * @param city          newly selected city
   * @param playersCities all the cities that belonged to the owner of the
   *                      newly selected city
   */
  void setSelectedCity(Game game, City city, ArrayList<City> playersCities) {
    // Store these last selections so this function can be called again later
    lastSelectedGame = game;
    lastSelectedCity = city;
    lastSelectedPlayersCities = playersCities;

    // Update the city details in the labels
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

    // Set the rename handler
    renameButton.setOnAction((e) -> {
      String newName = cityNameField.getText();
      // Update the name locally and broadcast a pakcet for the change
      city.name = newName;
      Civilisation.CLIENT.broadcast(new PacketCityRename(
        city.getX(),
        city.getY(),
        newName
      ));
    });

    // Iterate through every buildables' pane
    for (Map.Entry<CityBuildable, Pane> e : buildablePanes.entrySet()) {
      // Get UI components representing the buildable
      CityBuildable buildable = e.getKey();
      Pane buildablePane = e.getValue();
      Tooltip buildableTooltip = buildableTooltips.get(buildable);
      ProgressIndicator progressIndicator = buildableProgresses.get(buildable);

      // Toggle CSS class for opacity
      UIHelpers.toggleClass(
        buildablePane,
        "not-unlocked",
        !game.playerHasUnlocked(city.player.id, buildable)
      );
      // Toggle visibility
      buildablePane.setVisible(
        game.playerHasUnlocked(city.player.id, buildable)
      );
      // Prevents the pane from taking up space in the column if it's hidden
      buildablePane.setManaged(
        game.playerHasUnlocked(city.player.id, buildable)
      );

      // Whether this is the currently building item
      boolean currentlyBuildingThis = buildable.equals(city.currentlyBuilding);
      // Get the reason (if any) why this can't be built in the city
      String cantBuildReason = buildable.canBuildGivenCities(
        city,
        playersCities
      );

      // Calculate the tooltip text
      String tooltipText = "Click to build this in the city";
      boolean canBuild = true;
      if (currentlyBuildingThis) {
        tooltipText = "You are currently building this";
        canBuild = false;
      } else if (city.currentlyBuilding != null) {
        tooltipText = "You are currently building something else";
        canBuild = false;
      } else if (!buildWithProduction
        && game.getPlayerGoldTotal(city.player.id) < buildable.getGoldCost()) {
        tooltipText = "You don't have enough gold to build this";
        canBuild = false;
      } else if (cantBuildReason != null && cantBuildReason.length() > 0) {
        tooltipText = cantBuildReason;
        canBuild = false;
      }
      // Set the tooltip text to the calculate value
      buildableTooltip.setText(tooltipText);

      // Update classes for opacity
      UIHelpers.toggleClass(
        buildablePane,
        "can-build",
        canBuild
      );
      UIHelpers.toggleClass(
        buildablePane,
        "building",
        currentlyBuildingThis
      );

      // Set the progress of the current build
      if (currentlyBuildingThis) {
        progressIndicator.setProgress(
          Math.min(
            (double) city.productionTotal
              / (double) buildable.getProductionCost(),
            1.0
          )
        );
      }
      progressIndicator.setVisible(currentlyBuildingThis);

      // Add a click listener if this can be built
      if (canBuild) {
        buildablePane.setOnMouseClicked(event -> {
          // Create a packet detailing the build request
          PacketCityBuildRequest packetCityBuildRequest =
            new PacketCityBuildRequest(
              city.getX(),
              city.getY(),
              buildable,
              buildWithProduction
            );
          // Handle it locally and broadcast it to sync the state
          renderGame.handlePacket(packetCityBuildRequest);
          // Update the display of items now that something is being built
          setSelectedCity(game, city, playersCities);
          Civilisation.CLIENT.broadcast(packetCityBuildRequest);
          System.out.println("Clicked on " + buildable.getName());
        });
      } else {
        // Remove it if it can't
        buildablePane.setOnMouseClicked(null);
      }
    }
  }
}
