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

public class CivilisationServer implements Handler<Packet> {
  public static final Yaml YAML = new Yaml();

  private final File gameFile;
  private Game game;
  private Server<Packet> server;

  public CivilisationServer(String gameFileName, String gameName, MapSize mapSize, int port) throws IOException {
    this.gameFile = new File(gameFileName);
    game = new Game(gameName, mapSize);
    save();
    load();
    server = new Server<>(port, this);
  }

  public CivilisationServer(String gameFileName, int port) throws IOException {
    this.gameFile = new File(gameFileName);
    if(!this.gameFile.exists()) throw new IllegalArgumentException("game file doesn't exist");
    load();
    server = new Server<>(port, this);
  }

  public void close() throws IOException {
    server.close();
  }

  private void save() {
    try (FileWriter writer = new FileWriter(gameFile)) {
      YAML.dump(game.toMap(), writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void load() {
    try (FileReader reader = new FileReader(gameFile)) {
      //noinspection unchecked
      game = new Game(YAML.loadAs(reader, Map.class));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(Connection<Packet> connection, Packet data) {
    String id = connection.getId();
    if (data == null) {
      game.readyPlayers.put(id, false);
      return;
    }
    if (data instanceof PacketInit) {
      boolean shouldCreateStartingPackets = !game.containsPlayerWithId(id);

      PacketPlayerChange packetPlayerChange = new PacketPlayerChange(id);
      game.handlePacket(packetPlayerChange);
      connection.broadcastTo(new PacketGame(game.toMap()));
      connection.broadcastExcluding(packetPlayerChange);

      if (shouldCreateStartingPackets) {
        for (PacketUnitCreate packetUnitCreate : game.createStartingUnits(id)) {
          connection.broadcast(packetUnitCreate);
        }
      }

    } else if (data instanceof PacketReady) {
      game.readyPlayers.put(id, ((PacketReady) data).ready);
      if (game.allPlayersReady()) {
        PacketReady packetReady = new PacketReady(false);
        game.handlePacket(packetReady);
        connection.broadcast(packetReady);
      }
    } else if (data instanceof PacketUpdate) {
      Tile[] tilesToUpdate = game.handlePacket(data);
      if (tilesToUpdate != null && tilesToUpdate.length != 0) {
        for (Tile tile : tilesToUpdate) {
          if (tile.unit != null && tile.unit.isDead()) {
            game.units.remove(tile.unit);
            tile.unit = null;
          }
        }
      }
      connection.broadcastExcluding(data);
    }

    save();
  }

  public static void main(String[] args) throws IOException {
    new CivilisationServer("saves" + File.separator + "game.yml", "Game", MapSize.STANDARD, 1234);
  }
}
