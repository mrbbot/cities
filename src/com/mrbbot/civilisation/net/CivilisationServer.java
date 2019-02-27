package com.mrbbot.civilisation.net;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.MapSize;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.Connection;
import com.mrbbot.generic.net.Handler;
import com.mrbbot.generic.net.Server;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Class containing the implementation of the game server.
 */
public class CivilisationServer implements Handler<Packet> {
  /**
   * Instance of the external library used for saving/parsing YAML game saves.
   */
  public static final Yaml YAML = new Yaml();

  /**
   * File for the current game save the server is using
   */
  private final File gameFile;
  /**
   * The server instance of the game. Contains all state details but no render
   * references.
   */
  private Game game;
  /**
   * The instance of the generic server for sending/receiving {@link Packet}s.
   */
  private Server<Packet> server;

  /**
   * Creates a completely new game server with the specified details
   *
   * @param gameFileName path for the game save file
   * @param gameName     name of the new game
   * @param mapSize      map size of the new game
   * @param port         port number to run the server on
   * @throws IOException if there are any server networking errors
   */
  public CivilisationServer(
    String gameFileName,
    String gameName,
    MapSize mapSize,
    int port
  ) throws IOException {
    // Create the reference to the game file
    this.gameFile = new File(gameFileName);
    // Create the new game
    game = new Game(gameName, mapSize);
    // Save and then immediately load the game so it's in the same state as if
    // it were just loaded (see the 2nd constructor)
    save();
    load();
    // Start the server using this instance as the packet handler (See
    // accept(Connection<Packet> Packet)).
    server = new Server<>(port, this);
  }

  /**
   * Creates a new game server loaded from an existing game save
   *
   * @param gameFileName path of the game save file
   * @param port         port number to run the server on
   * @throws IOException if there are any server networking errors
   */
  public CivilisationServer(String gameFileName, int port) throws IOException {
    // Create the reference to the game file
    this.gameFile = new File(gameFileName);
    // Check the save exists
    if (!this.gameFile.exists())
      throw new IllegalArgumentException("game file doesn't exist");
    // Load the game
    load();
    // Start the server using this instance as the packet handler (See
    // accept(Connection<Packet> Packet)).
    server = new Server<>(port, this);
  }

  /**
   * Closes the server's socket disconnecting all clients
   *
   * @throws IOException if there are any networking errors
   */
  public void close() throws IOException {
    server.close();
  }

  /**
   * Saves the game state to the game file
   */
  private void save() {
    // Try and create a file writer, closing it when the game state has been
    // written
    try (FileWriter writer = new FileWriter(gameFile)) {
      // Dump the game state as YAML
      YAML.dump(game.toMap(), writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Loads and overwrites the game state from the game file
   */
  private void load() {
    // Try and create a file reader, closing it when the game state has been
    // read
    try (FileReader reader = new FileReader(gameFile)) {
      // Read the game state as YAML
      //noinspection unchecked
      game = new Game(YAML.loadAs(reader, Map.class));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Main packet handler for the server
   *
   * @param connection connection object for a client
   * @param data       packet the client has just sent, may be null if the
   *                   client has disconnected
   */
  @Override
  public void accept(Connection<Packet> connection, Packet data) {
    // Alias the connection id
    String id = connection.getId();
    if (data == null) {
      // If the player disconnects, mark them as not ready so the game waits
      // for them to reconnect
      game.readyPlayers.put(id, false);
      return;
    }
    // Otherwise depending on the type of packet...
    if (data instanceof PacketInit) {
      // Check if this is the first time the player has joined this game
      boolean shouldCreateStartingPackets = !game.containsPlayerWithId(id);

      // Broadcast the player change to every other client
      PacketPlayerChange packetPlayerChange = new PacketPlayerChange(id);
      game.handlePacket(packetPlayerChange);
      // Send the current game state to the new player
      connection.broadcastTo(new PacketGame(game.toMap()));
      connection.broadcastExcluding(packetPlayerChange);

      // Create the starting units (initial settler and warrior) if this is the
      // first time the player has joined this game.
      if (shouldCreateStartingPackets) {
        for (PacketUnitCreate packet : game.createStartingUnits(id)) {
          // Broadcast them to every client, not just the new player
          connection.broadcast(packet);
        }
      }
    } else if (data instanceof PacketReady) {
      // Set the players ready state
      game.readyPlayers.put(id, ((PacketReady) data).ready);
      // Check if all players have marked themselves as ready
      if (game.allPlayersReady()) {
        // Handle the turn and request all clients do the same
        PacketReady packetReady = new PacketReady(false);
        game.handlePacket(packetReady);
        connection.broadcast(packetReady);
      }
    } else if (data instanceof PacketUpdate) {
      // If this was a game state update, update the local state
      Tile[] tilesToUpdate = game.handlePacket(data);
      // Check if any units have died and remove them from the game
      if (tilesToUpdate != null && tilesToUpdate.length != 0) {
        for (Tile tile : tilesToUpdate) {
          if (tile.unit != null && tile.unit.isDead()) {
            game.units.remove(tile.unit);
            tile.unit = null;
          }
        }
      }
      // Send the update to all connected clients but the sender
      connection.broadcastExcluding(data);
    }

    // Save the game state to the file after handling the packet so the game
    // can be easily restored
    save();
  }

  /**
   * Entry point for a dedicated server (one without a UI/client)
   *
   * @param args command line arguments
   * @throws IOException if there are any server networking errors
   */
  public static void main(String[] args) throws IOException {
    new CivilisationServer(
      "saves" + File.separator + "game.yml",
      "Game",
      MapSize.STANDARD,
      1234
    );
  }
}
