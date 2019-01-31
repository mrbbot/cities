package com.mrbbot.generic.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Server<T> extends BaseBroadcaster<T> implements Runnable {
  private final ServerSocket serverSocket;
  private final Handler<T> handler;
  private HashMap<String, Connection> connections;

  public Server(int port, Handler<T> handler) throws IOException {
    this.serverSocket = new ServerSocket(port);
    this.handler = handler;
    this.connections = new HashMap<>();

    Thread thread = new Thread(this, "Server");
    thread.start();
  }

  public void broadcast(T data) {
    broadcastWhere(data, (id) -> true);
  }

  public void broadcastWhere(T data, Predicate<String> test) {
    for (Map.Entry<String, Connection> connection : connections.entrySet()) {
      if (test.test(connection.getKey())) {
        try {
          connection.getValue().send(data);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void run() {
    boolean open = true;
    while (open) {
      try {
        Socket socket = serverSocket.accept();
        new Connection<>(
          socket,
          (connection, id) -> {
            //TODO: check if ID is already taken
            connection.setId(id);
            connections.put(id, connection);
            try {
              connection.send("Server");
            } catch (IOException e) {
              e.printStackTrace();
            }
          },
          (connection, data) -> {
            if (data == null) {
              connections.remove(connection.getId());
            } else {
              //noinspection unchecked
              handler.accept(connection, data);
            }
          },
          this
        );
      } catch (IOException e) {
        e.printStackTrace();
        open = false;
      }
    }
  }
}
