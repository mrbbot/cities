package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.net.packet.PacketChat;
import com.mrbbot.civilisation.net.packet.PacketReady;
import com.mrbbot.civilisation.render.RenderCivilisation;
import com.mrbbot.civilisation.render.map.RenderGame;
import com.mrbbot.civilisation.ui.Screen;
import com.mrbbot.civilisation.ui.UIHelpers;
import com.mrbbot.generic.net.ClientOnly;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main screen for the game. Contains the game render and UI overlays.
 */
@ClientOnly
public class ScreenGame extends Screen {
  /**
   * Game that should be rendered in this screen
   */
  private final Game game;
  /**
   * ID of the current game player
   */
  private final String id;
  /**
   * Root render object
   */
  public RenderCivilisation renderCivilisation;
  /**
   * Panel containing UI overlays for player stats, research progress, unit
   * selection details, city production list, etc
   */
  private UIGame ui;

  /**
   * Creates a new game screen
   *
   * @param game game that should be rendered by the screen
   * @param id   ID of the player who's currently playing the game
   */
  public ScreenGame(Game game, String id) {
    // Store the values so they can be used later
    this.game = game;
    this.id = id;
  }

  /**
   * Creates a scene representing this screen
   *
   * @param stage  stage the scene would be placed in
   * @param width  width of the screen
   * @param height height of the screen
   * @return scene representing this screen
   */
  @Override
  public Scene makeScene(Stage stage, int width, int height) {
    StackPane pane = new StackPane();
    pane.setAlignment(Pos.CENTER);

    // Create a render object for the game
    RenderGame renderGame = new RenderGame(
      game,
      id,
      // Register unit and city selection listeners
      (unit) -> ui.onSelectedUnitChanged(game, unit),
      (city) -> ui.onSelectedCityChanged(
        game,
        city,
        game.getPlayersCitiesById(id)
      )
    );
    // Create the root render object allowing for zooming, panning, and
    // lighting
    this.renderCivilisation = new RenderCivilisation(
      renderGame,
      width,
      height
    );

    // Create the game UI
    ui = new UIGame(renderGame, height);
    ui.setPrefSize(width, height);

    // Register game state listeners so changes can be reflected in the UI
    game.setCurrentPlayer(
      id,
      (stats) -> ui.onPlayerStatsChanged(stats)
    );
    game.setTechDetailsListener(
      (details) -> ui.onTechDetailsChanged(game, details)
    );
    // Show a dialog on new messages (research unlocks, errors, etc)
    game.setMessageListener(UIHelpers::showDialog);

    // Add the 3D render's sub-scene to the pane
    pane.getChildren().addAll(this.renderCivilisation.subScene, ui);

    // Create a new scene
    Scene scene = new Scene(pane, width, height);
    // Register a CSS stylesheet for styling some of the UI panels (mostly
    // the city production list)
    scene.getStylesheets().add("/com/mrbbot/civilisation/ui/game/styles.css");
    // Set the scene of the render, registering keyboard shortcuts
    this.renderCivilisation.setScene(scene, e -> {
      if (e.getCode() == KeyCode.F11) {
        stage.setFullScreen(!stage.isFullScreen());
      }
    });
    return scene;
  }

  /**
   * Forwards a chat packet to the UI, so it can be displayed
   *
   * @param packet packet to forward
   */
  public void handlePacketChat(PacketChat packet) {
    ui.handlePacketChat(packet);
  }

  /**
   * Forwards a ready packet to the UI, so the next turn button can be
   * re-enabled
   *
   * @param packet packet to forward
   */
  public void handlePacketReady(PacketReady packet) {
    ui.handlePacketReady(packet);
  }
}
