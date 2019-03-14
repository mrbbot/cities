package com.mrbbot.civilisation;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.MapSize;
import com.mrbbot.civilisation.net.CivilisationServer;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.civilisation.ui.connect.ClientCreator;
import com.mrbbot.civilisation.ui.connect.ScreenConnect;
import com.mrbbot.civilisation.ui.connect.ServerCreator;
import com.mrbbot.civilisation.ui.game.ScreenGame;
import com.mrbbot.generic.net.Client;
import com.mrbbot.generic.net.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Class containing the main entry point for the program.
 */
public class Civilisation extends Application
  implements ClientCreator, ServerCreator {
  /**
   * The game client. Exposed so that any component of the game can send
   * packets to the server.
   */
  public static Client<Packet> CLIENT;
  /**
   * The internal game server. There should only be one instance of this per
   * instance of the program.
   */
  private static CivilisationServer SERVER;

  /**
   * Primary stage of the application. This is where the scenes for the
   * different screens go.
   */
  private Stage primaryStage;
  /**
   * Width of the application window.
   */
  private int width;
  /**
   * Height of the application window.
   */
  private int height;
  /**
   * The game screen for this client. Contains the 3D map render and the UI
   * information overlays.
   */
  private ScreenGame screenGame;

  @Override
  public void start(Stage primaryStage) {
    // Store the primary stage so the screen can be changed later.
    this.primaryStage = primaryStage;

    // Make the game occupy all of the screen
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    width = (int) screenBounds.getWidth();
    height = (int) screenBounds.getHeight();

    // Create the initial connection screen, registering this as the client
    // and server creator
    ScreenConnect screenConnect = new ScreenConnect(
      this, this
    );
    // Show the connection screen by default
    primaryStage.setScene(
      screenConnect.makeScene(primaryStage, width, height)
    );

    // Set window details
    primaryStage.setTitle("Civilisation");
    primaryStage.setResizable(false);
    //primaryStage.setFullScreen(true);

    // Terminate the client and server when the user requests the game exit by
    // clicking the window's close button
    primaryStage.setOnCloseRequest((event) -> {
      try {
        if (CLIENT != null) CLIENT.close();
      } catch (IOException ignored) {
      }
      try {
        if (SERVER != null) SERVER.close();
      } catch (IOException ignored) {
      }
      System.exit(0);
    });

    // Show the game window
    primaryStage.show();
  }

  /**
   * Function to create a new game client. Creates and shows the game screen
   * when the first packet is received.
   *
   * @param host server host IP/URL
   * @param port server port number
   * @param id   desired id of the player
   * @throws IOException if there was a networking error
   */
  public void createClient(
    String host,
    int port,
    String id
  ) throws IOException {
    // Store the client so that all components of the game can send packets to
    // the server
    CLIENT = new Client<>(
      host,
      port,
      id,
      // Run the packet handler on the UI thread so that UI components can be
      // updated without throwing errors
      ((connection, data) -> Platform.runLater(() -> {
        if (data instanceof PacketGame) {
          // If the packet contains game state information (1st packet), create
          // the game screen with the existing state and show it to the user
          Game game = new Game(((PacketGame) data).map);
          screenGame = new ScreenGame(game, id);
          primaryStage.setScene(
            screenGame.makeScene(primaryStage, width, height)
          );
        } else if (data instanceof PacketChat) {
          // If this was a chat packet, send it to the chat panel
          screenGame.handlePacketChat((PacketChat) data);
        } else {
          // Otherwise, if it was anything else...

          // If this was a ready packet, make the "Next Turn" button clickable
          // again
          if (data instanceof PacketReady) {
            screenGame.handlePacketReady((PacketReady) data);
          }

          // Get the game to handle it (likely a game state sync [unit moving,
          // city creation, etc])
          screenGame.renderCivilisation.root.handlePacket(data);
        }
      }))
    );
    // Send a request for the current game state
    CLIENT.broadcast(new PacketInit());
  }

  /**
   * Function to create a new internal game server.
   *
   * @param gameFilePath file path of the game save file (may or may not exist)
   * @param gameName     name of the game (if this is null, we're loading an
   *                     existing game from a file)
   * @param mapSize      desired map size of the new game (ignored if loading
   *                     from a file)
   * @param port         port number to run the server on
   * @throws IOException if there was a networking error
   */
  public void createServer(
    String gameFilePath,
    String gameName,
    MapSize mapSize,
    int port
  ) throws IOException {
    if (gameName == null) {
      // If the game name is null, we're loading an existing game from a file
      // so don't pass the additional parameters
      SERVER = new CivilisationServer(gameFilePath, port);
    } else {
      // Otherwise, create an entirely new game
      SERVER = new CivilisationServer(gameFilePath, gameName, mapSize, port);
    }
  }

  /**
   * Main entry point for the client program. Launches the JavaFX application.
   * @param args command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
}

