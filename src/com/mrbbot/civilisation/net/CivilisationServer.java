package com.mrbbot.civilisation.net;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.Connection;
import com.mrbbot.generic.net.Handler;
import com.mrbbot.generic.net.Server;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class CivilisationServer implements Handler<Packet> {
  private static Yaml YAML;
  static {
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setSplitLines(false);
    YAML = new Yaml(dumperOptions);
  }

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
    if(data == null) return;
    String id = connection.getId();
    System.out.println("Received \"" + data.getClass().getTypeName() + "\" packet from \"" + id + "\"...");
    if (data instanceof PacketInit) {
      boolean shouldCreateStartingPackets = !game.containsPlayerWithId(id);

      PacketPlayerChange packetPlayerChange = new PacketPlayerChange(id);
      game.handlePacket(packetPlayerChange);
      connection.broadcastTo(new PacketGame(game.toMap()));
      connection.broadcastExcluding(packetPlayerChange);

      if(shouldCreateStartingPackets) {
        for (PacketUnitCreate packetUnitCreate : game.createStartingUnits(id)) {
          connection.broadcast(packetUnitCreate);
        }
      }

    } else if (data instanceof PacketUpdate) {
      game.handlePacket(data);
      connection.broadcastExcluding(data);
    }
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

  public static void main(String[] args) throws IOException {
    new CivilisationServer();
  }
}
