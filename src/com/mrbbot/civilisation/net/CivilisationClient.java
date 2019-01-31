package com.mrbbot.civilisation.net;

import com.mrbbot.civilisation.net.packet.Packet;
import com.mrbbot.civilisation.net.packet.PacketInit;
import com.mrbbot.civilisation.net.packet.PacketMap;
import com.mrbbot.generic.net.Client;

import java.io.IOException;

public class CivilisationClient {
  public static void main(String[] args) throws IOException {
    Client<Packet> chatClient = new Client<>("127.0.0.1", 1234, "someid", ((connection, data) -> {
      System.out.println("Received " + data.getName() + " packet...");

      if(data instanceof PacketMap) {
        System.out.println(((PacketMap) data).map.hexagonGrid.get(2, 2).x);
      }
    }));
    chatClient.broadcast(new PacketInit());
  }
}
