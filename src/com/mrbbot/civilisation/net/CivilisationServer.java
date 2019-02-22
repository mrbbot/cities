package com.mrbbot.civilisation.net;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.Connection;
import com.mrbbot.generic.net.Handler;
import com.mrbbot.generic.net.Server;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class CivilisationServer implements Handler<Packet> {
  private static final Yaml YAML = new Yaml();

  /*static {
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setSplitLines(false);
    YAML = new Yaml(dumperOptions);
  }*/

  private Game game;

  private CivilisationServer() throws IOException {
    game = new Game("Game");
    //TODO: remove these, they're just for testing
    save("game.yml");
    load("game.yml");

    new Server<>(1234, this);
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

    save("game.yml");
  }

  private void save(String fileName) {
    try (FileWriter writer = new FileWriter(fileName)) {
      YAML.dump(game.toMap(), writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void load(String fileName) {
    try (FileReader reader = new FileReader(fileName)) {
      //noinspection unchecked
      game = new Game(YAML.loadAs(reader, Map.class));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //TODO: move this into the client
  public static void main(String[] args) throws IOException {
    new CivilisationServer();
  }
}
