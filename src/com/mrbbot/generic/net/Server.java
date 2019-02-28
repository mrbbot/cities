package com.mrbbot.generic.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Generic server class for listening for clients' connections and data
 *
 * @param <T> type of data to be exchanged over the network
 */
public class Server<T> extends BaseBroadcaster<T> implements Runnable {
  /**
   * TCP server socket to listen for incoming connections on
   */
  private final ServerSocket serverSocket;
  /**
   * Handler function for incoming data coming from clients
   */
  private final Handler<T> handler;
  /**
   * Map mapping connection IDs to their connection objects
   */
  private HashMap<String, Connection> connections;

  /**
   * Creates a new server
   *
   * @param port    port number to listen for connections on
   * @param handler function to be called when data is received from a client
   * @throws IOException if the server socket cannot be created (likely
   *                     because the port has already be bound)
   */
  public Server(int port, Handler<T> handler) throws IOException {
    // Create the TCP server socket
    this.serverSocket = new ServerSocket(port);
    // Store the handler so it can be called later
    this.handler = handler;
    // Initialise the connections map
    this.connections = new HashMap<>();

    // Create a new thread for handling incoming connections from clients
    Thread thread = new Thread(this, "Server");
    thread.start();
  }

  /**
   * Broadcasts data to all connections
   *
   * @param data data to be sent
   */
  public void broadcast(T data) {
    broadcastWhere(data, (id) -> true);
  }

  /**
   * Broadcasts data to connections that return true from the predicate
   *
   * @param data data to be sent
   * @param test function to test each connection ID against, if it returns
   *             true, the data is sent to that connection ID
   */
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

  /**
   * Function that runs the server. Called in a separate thread.
   */
  @Override
  public void run() {
    boolean open = true;
    // Whilst the socket is open...
    while (open) {
      try {
        // Listen for new connections, this blocks until a new connection
        // request is received or the socket is closed
        Socket socket = serverSocket.accept();
        // Create a connection object that waits for data from the socket and
        // facilitates sending data to the client
        new Connection<>(
          socket,
          (connection, id) -> {
            // Set the connection ID when it is sent
            connection.setId(id);
            // Store the connection in the map
            connections.put(id, connection);
            try {
              // Send the server's ID
              connection.send("Server");
            } catch (IOException e) {
              e.printStackTrace();
            }
          },
          (connection, data) -> {
            // If the connection's been closed (data is null)
            if (data == null) {
              // Remove the connection from the map
              connections.remove(connection.getId());
            }
            // Forward the incoming data onto the incoming data handler
            handler.accept(connection, data);
          },
          this
        );
      } catch (IOException e) {
        // Ignore the error if it was just the socket closing
        if (!e.getMessage().equals("socket closed")) {
          e.printStackTrace();
        }
        open = false;
      }
    }
  }

  /**
   * Closes the servers's socket, disconnecting all connected clients
   *
   * @throws IOException if there was a networking error
   */
  public void close() throws IOException {
    serverSocket.close();
  }
}
