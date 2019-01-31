package com.mrbbot.generic.net;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Predicate;

public class Client<T> extends BaseBroadcaster<T> {
  private Connection<T> connection;
  private Socket socket;

  public Client(String host, int port, String id, Handler<T> handler) throws IOException {
    socket = new Socket(host, port);
    connection = new Connection<>(
      socket,
      Connection::setId,
      (connection, data) -> {
        if (data != null) {
          handler.accept(connection, data);
        }
      },
      this
    );
    connection.send(id);
  }

  @Override
  public void broadcast(T data) {
    try {
      connection.send(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void broadcastWhere(T data, Predicate<String> test) {
    if (test.test(connection.getId())) {
      try {
        connection.send(data);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void close() throws IOException {
    socket.close();
  }
}
