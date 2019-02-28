package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.PlayerStats;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.techs.PlayerTechDetails;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.civilisation.render.map.RenderGame;
import com.mrbbot.generic.net.ClientOnly;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * UI panel containing all other UI panels for the game (stats, techs, etc).
 * Extends anchor pane allowing items to be positioned relative to the edges of
 * the screen.
 */
@ClientOnly
public class UIGame extends AnchorPane {
  /**
   * Default amount of padding for UI panels
   */
  private static final Insets PANEL_PADDING = new Insets(10);

  /**
   * Game render object reference for performing actions on the game state.
   */
  private final RenderGame renderGame;
  /**
   * Colour of the current player. Used for panel borders.
   */
  private final Color playerColor;

  /**
   * Panel for technology stats (current research and progress)
   */
  private final UIPanelTech panelTech;
  /**
   * Panel for chat messages and for sending new ones
   */
  private final UIPanelChat panelChat;
  /**
   * Panel for performing actions with units and displaying information on the
   * current selection
   */
  private final UIPanelActions panelActions;
  /**
   * Panel for current player statistics (gold/science per turn)
   */
  private final UIPanelStats panelStats;
  /**
   * UI of the technology tree showing techs available for research and
   * previously researched items.
   */
  private final UITechTree techTree;
  /**
   * UI panel for showing city details and for choosing what to build in a
   * city.
   */
  private final UIPanelCityDetails panelCityDetails;
  /**
   * Button that when clicked closes the tech tree. The open button is in
   * {@link UIPanelTech}.
   */
  private final Button closeTechTreeButton;

  /**
   * Creates a new game UI instance
   *
   * @param renderGame reference to the current game render object
   * @param height     height of the screen this is displayed in
   */
  public UIGame(RenderGame renderGame, int height) {
    this.renderGame = renderGame;
    // Stop preventing the mouse from reaching the 3D render object that would
    // prevent panning, zooming, and unit selection.
    setPickOnBounds(false);

    Player player = this.renderGame.currentPlayer;
    playerColor = player.getColour();

    // Create the tech panel
    panelTech = new UIPanelTech();
    panelTech.setBorder(makePanelBorder(Pos.BOTTOM_RIGHT));
    panelTech.setBackground(makePanelBackground(Pos.BOTTOM_RIGHT));
    panelTech.setPadding(PANEL_PADDING);
    // Open the tech tree on clicking the open button
    panelTech.setOnOpenTechTree(e -> setTechTreeVisible(true));
    // Position it in the top left of the screen
    AnchorPane.setTopAnchor(panelTech, 0.0);
    AnchorPane.setLeftAnchor(panelTech, 0.0);

    // Create the chat panel
    panelChat = new UIPanelChat(renderGame.currentPlayer.id);
    panelChat.setBorder(makePanelBorder(Pos.BOTTOM_LEFT));
    panelChat.setBackground(makePanelBackground(Pos.BOTTOM_LEFT));
    panelChat.setPadding(PANEL_PADDING);
    // Position it in the top right of the screen
    AnchorPane.setTopAnchor(panelChat, 0.0);
    AnchorPane.setRightAnchor(panelChat, 0.0);

    // Create the unit actions panel
    panelActions = new UIPanelActions();
    panelActions.setBorder(makePanelBorder(Pos.TOP_LEFT));
    panelActions.setBackground(makePanelBackground(Pos.TOP_LEFT));
    panelActions.setPadding(PANEL_PADDING);
    // Register the listener for when the user requests a unit take an action
    // or clicks on the next turn button
    panelActions.setUnitActionListener(this::onUnitAction);
    // Position it in the bottom right of the screen
    AnchorPane.setBottomAnchor(panelActions, 0.0);
    AnchorPane.setRightAnchor(panelActions, 0.0);

    // Create the player stats panel
    panelStats = new UIPanelStats();
    panelStats.setBorder(makePanelBorder(Pos.TOP_RIGHT));
    panelStats.setBackground(makePanelBackground(Pos.TOP_RIGHT));
    panelStats.setPadding(PANEL_PADDING);
    // Position it in the bottom left of the screen
    AnchorPane.setBottomAnchor(panelStats, 0.0);
    AnchorPane.setLeftAnchor(panelStats, 0.0);

    // Create the city details panel
    panelCityDetails = new UIPanelCityDetails(renderGame);
    panelCityDetails.setBorder(new Border(new BorderStroke(
      playerColor,
      BorderStrokeStyle.SOLID,
      CornerRadii.EMPTY,
      // Left border only
      new BorderWidths(0, 0, 0, 10)
    )));
    panelCityDetails.setBackground(makePanelBackground(Pos.CENTER));
    panelCityDetails.setVisible(false);
    // Position it to the right of the screen taking up the full screen height
    AnchorPane.setTopAnchor(panelCityDetails, 0.0);
    AnchorPane.setRightAnchor(panelCityDetails, 0.0);
    AnchorPane.setBottomAnchor(panelCityDetails, 0.0);

    // Create initial player technology details for initialising the tech tree
    PlayerTechDetails techDetails = new PlayerTechDetails(
      renderGame.data.getPlayerUnlockedTechs(player.id),
      renderGame.data.getPlayerUnlockingTech(player.id),
      renderGame.data.getPlayerUnlockingProgress(player.id)
    );
    // Create the tech tree UI
    techTree = new UITechTree(
      renderGame.data,
      player.id,
      techDetails,
      height
    );
    techTree.setBorder(makePanelBorder(Pos.CENTER));
    // Fill the screen with the tech tree when it's visible
    AnchorPane.setTopAnchor(techTree, 0.0);
    AnchorPane.setLeftAnchor(techTree, 0.0);
    AnchorPane.setBottomAnchor(techTree, 0.0);
    AnchorPane.setRightAnchor(techTree, 0.0);
    // Set the initial tech details
    panelTech.setTechDetails(techDetails);

    // Create the close button
    closeTechTreeButton = new Button("Close Tech Tree");
    closeTechTreeButton.setOnAction(e -> setTechTreeVisible(false));
    // Position it in the top left of the screen
    AnchorPane.setTopAnchor(closeTechTreeButton, 20.0);
    AnchorPane.setLeftAnchor(closeTechTreeButton, 20.0);

    // Hide the tech tree initially
    setTechTreeVisible(false);

    // Add all the panels to the screen
    getChildren().addAll(
      panelTech,
      panelChat,
      panelActions,
      panelStats,
      panelCityDetails,
      techTree,
      closeTechTreeButton
    );
  }

  /**
   * Create a corner radii object with the radius in the corner specified
   *
   * @param cutout corner for the cutout
   * @param size   size of the cutout
   * @return corner radii object with details on the corner cutout
   */
  private CornerRadii makeCornerRadiiForCutout(Pos cutout, int size) {
    return new CornerRadii(
      cutout == Pos.TOP_LEFT ? size : 0,
      cutout == Pos.TOP_RIGHT ? size : 0,
      cutout == Pos.BOTTOM_RIGHT ? size : 0,
      cutout == Pos.BOTTOM_LEFT ? size : 0,
      false
    );
  }

  /**
   * Makes a border object with a cutout in the specified corner
   *
   * @param cutout corner for the cutout
   * @return border object with a cutout
   */
  private Border makePanelBorder(Pos cutout) {
    return new Border(new BorderStroke(
      playerColor,
      BorderStrokeStyle.SOLID,
      makeCornerRadiiForCutout(cutout, 10),
      new BorderWidths(10)
    ));
  }

  /**
   * Makes a solid white background with a cutout in the specified corner
   *
   * @param cutout corner for the cutout
   * @return background object with a cutout
   */
  private Background makePanelBackground(Pos cutout) {
    return new Background(new BackgroundFill(
      Color.WHITE,
      makeCornerRadiiForCutout(cutout, 20),
      null
    ));
  }

  /**
   * Callback function called when the selected unit changes in the game
   *
   * @param game game containing the unit
   * @param unit selected unit, may be null if no unit is selected
   */
  void onSelectedUnitChanged(Game game, Unit unit) {
    panelActions.setSelectedUnit(game, unit);
  }

  /**
   * Callback function called when the selected city changes in the game
   *
   * @param game          game containing the city
   * @param city          selected city, may be null if no unit is selected
   * @param playersCities all of the players cities in the game
   */
  void onSelectedCityChanged(
    Game game,
    City city,
    ArrayList<City> playersCities
  ) {
    // Update the city details panel to reflect the change if required
    if (city != null)
      panelCityDetails.setSelectedCity(game, city, playersCities);
    // Show/hide the panel depending on if a city has been selected or not
    panelCityDetails.setVisible(city != null);
  }

  /**
   * Callback function called when the player requests a unit perform an action
   * or clicks the the next turn button.
   *
   * @param unit          unit the action should be performed on or null if the
   *                      next turn button was pressed
   * @param actionDetails string containing additional details about the action
   *                      (i.e. what improvement a worker should construct)
   */
  private void onUnitAction(Unit unit, String actionDetails) {
    // Check if this was the next turn button
    if (unit == null) {
      System.out.println("Next turn...");
      // Mark the client as waiting for other players, so it can't perform any
      // more actions
      renderGame.data.waitingForPlayers = true;
      // Deselect any units/cities
      renderGame.setSelectedUnit(null);
      renderGame.setSelectedCity(null);
    } else {
      // Otherwise, an action is to be performed
      System.out.printf(
        "%s performed an action (details \"%s\")\n",
        unit.unitType.getName(),
        actionDetails
      );
      if (unit.hasAbility(UnitAbility.ABILITY_SETTLE)) {
        // If this unit was a settler, try and create a city on the unit's tile

        // Broadcast a packet...
        Civilisation.CLIENT.broadcast(new PacketCityCreate(
          renderGame.currentPlayer.id,
          unit.tile.x,
          unit.tile.y
        ));

        // ...and create the city for this client
        renderGame.data.cities.add(new City(
          renderGame.data.hexagonGrid,
          unit.tile.x,
          unit.tile.y,
          renderGame.currentPlayer
        ));
        // Rerender every tile
        renderGame.updateTileRenders();
        renderGame.setSelectedUnit(null);
        // Settlers can only be used once, so delete this unit
        renderGame.deleteUnit(unit, true);
      } else if (unit.hasAbility(UnitAbility.ABILITY_IMPROVE)) {
        // If this unit was a worker, try and improve the unit's tile

        // Get the improvement from the action's details
        Improvement improvement = Improvement.fromName(actionDetails);
        assert improvement != null;

        // Create a packet detailing the request
        PacketWorkerImproveRequest packetWorkerImproveRequest =
          new PacketWorkerImproveRequest(
            unit.tile.x,
            unit.tile.y,
            improvement
          );
        // Handle it locally and broadcast it so other clients stay in sync
        renderGame.data.handlePacket(packetWorkerImproveRequest);
        Civilisation.CLIENT.broadcast(packetWorkerImproveRequest);
        renderGame.setSelectedUnit(null);
      } else if (unit.unitType.getUpgrade() != null) {
        // If this unit could be upgraded, upgrade the unit

        // Create a packet detailing the request
        PacketUnitUpgrade packetUnitUpgrade = new PacketUnitUpgrade(
          unit.tile.x,
          unit.tile.y
        );
        // Handle it locally and broadcast it so other clients stay in sync
        renderGame.data.handlePacket(packetUnitUpgrade);
        Civilisation.CLIENT.broadcast(packetUnitUpgrade);
        renderGame.setSelectedUnit(null);
      } else if (unit.hasAbility(UnitAbility.ABILITY_BLAST_OFF)) {
        // If this is a rocket, blast off and win the game

        // Create a packet detailing the request
        PacketBlastOff packetBlastOff = new PacketBlastOff(
          renderGame.currentPlayer.id
        );
        // Handle it locally and broadcast it so other clients stay in sync
        renderGame.handlePacket(packetBlastOff);
        Civilisation.CLIENT.broadcast(packetBlastOff);
        renderGame.setSelectedUnit(null);
        // Rockets can only be used once, so delete this unit
        renderGame.deleteUnit(unit, true);
      }
    }
  }

  /**
   * Sets the tech tree's visibility
   *
   * @param visible whether the tech tree should be visible
   */
  private void setTechTreeVisible(boolean visible) {
    techTree.setVisible(visible);
    closeTechTreeButton.setVisible(visible);
  }

  /**
   * Callback function for a new chat packet. Adds the new chat message to the
   * chat log.
   *
   * @param packet packet containing the chat message
   */
  void handlePacketChat(PacketChat packet) {
    panelChat.addMessage(packet.message);
  }

  /**
   * Callback function for a ready packet. Resets the action panel's next turn
   * button allowing it to be clicked again.
   *
   * @param data packet containing turn ready information
   */
  void handlePacketReady(PacketReady data) {
    panelActions.setNextTurnWaiting(data.ready);
  }

  /**
   * Callback function called when a player's stats change (usually once per
   * turn)
   *
   * @param stats new stats for the current player
   */
  void onPlayerStatsChanged(PlayerStats stats) {
    panelStats.setPlayerStats(stats);
  }

  /**
   * Callback function called when a player's tech details changed (usually
   * once per turn)
   *
   * @param game    game containing the player
   * @param details new tech details for the current player
   */
  void onTechDetailsChanged(Game game, PlayerTechDetails details) {
    panelTech.setTechDetails(details);
    techTree.setTechDetails(game, details);
  }
}
