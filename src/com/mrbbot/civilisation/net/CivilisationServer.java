package com.mrbbot.civilisation.net;

import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.net.packet.Packet;
import com.mrbbot.civilisation.net.packet.PacketInit;
import com.mrbbot.civilisation.net.packet.PacketMap;
import com.mrbbot.civilisation.net.packet.PacketUnitMove;
import com.mrbbot.generic.net.Server;

import java.io.IOException;

public class CivilisationServer {
  public static void main(String[] args) throws IOException {
    final Map map = new Map();

    new Server<Packet>(1234, ((connection, data) -> {
      System.out.println("Received \"" + data.getName() + "\" packet from \"" + connection.getId() + "\"...");

      if(data instanceof PacketInit) {
        connection.broadcastTo(new PacketMap(map));
      } else if(data instanceof PacketUnitMove) {
        connection.broadcastExcluding(data);
      }
    }));
  }
}
