package com.mrbbot.civilisation.net;

import com.mrbbot.generic.net.Server;

import java.io.IOException;

public class ChatServer {
  public static void main(String[] args) throws IOException {
    new Server<String>(1234, (connection, data) -> connection.broadcastExcluding(connection.getId()+ ": " + data));
  }
}
