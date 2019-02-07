package com.mrbbot.civilisation.net;

import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.Server;

import java.io.IOException;

public class CivilisationServer {
  public static void main(String[] args) throws IOException {
    final Map map = new Map();

    new Server<Packet>(1234, ((connection, data) -> {
      String id = connection.getId();
      if (data == null) {
        connection.broadcastExcluding(new PacketPlayerChange(id, false));
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
