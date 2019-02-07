package com.mrbbot.civilisation.net;

import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.Server;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

public class CivilisationServer {
  public static void main(String[] args) throws IOException {
    final Map map = new Map();

    Yaml yaml = new Yaml();
    String output = yaml.dump(map.toMap());
    System.out.println(output);

    new Server<Packet>(1234, ((connection, data) -> {
      String id = connection.getId();
      if (data == null) {
        connection.broadcastExcluding(new PacketPlayerChange(id, false));
        map.players.removeIf((player -> player.id.equals(id)));
        System.out.println(map.players.size());
        return;
      }

      System.out.println("Received \"" + data.getName() + "\" packet from \"" + id + "\"...");

      if (data instanceof PacketInit) {
        map.players.add(new Player(id));
        connection.broadcastTo(new PacketMap(map));
        connection.broadcastExcluding(new PacketPlayerChange(id, true));
      } else if (data instanceof PacketUpdate) {
        connection.broadcastExcluding(data);
      }
    }));
  }
}
