package com.mrbbot.generic.net;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Predicate;

/**
 * Generic client class for connecting to a generic server and exchanging data
 *
 * @param <T> type of data to be exchanged over the network
 */
public class Client<T> extends BaseBroadcaster<T> {
  /**
   * Connection object for the client representing the connection to the server
   */
  private Connection<T> connection;
  /**
   * TCP socket for transferring data to and from the server
   */
  private Socket socket;

  /**
   * Creates a new client and connects to the specified server
   *
   * @param host    host name of the server
   * @param port    port number the server is listening on
   * @param id      id for this connection
   * @param handler data handler for when data is received from the server
   * @throws IOException if there was a connection error
   */
  public Client(
    String host,
    int port,
    String id,
    Handler<T> handler
  ) throws IOException {
    // Create the TCP socket
    socket = new Socket(host, port);
    // Create a connection object that waits for data from the socket and
    // facilitates sending data to the server
    connection = new Connection<>(
      socket,
      // Set the ID of the connection when one is received
      Connection::setId,
      (connection, data) -> {
        if (data != null) {
          // Pass data to the handler if it isn't null
          handler.accept(connection, data);
        }
      },
      this
    );
    // Send the desired ID as the first packet
    connection.send(id);
  }

  /**
   * Broadcasts data to the server
   *
   * @param data data to be sent
   */
  @Override
  public void broadcast(T data) {
    try {
      connection.send(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Broadcasts data to to the server if the connection ID matches the
   * predicate
   *
   * @param data data to be sent
   * @param test function to test each connection ID against, if it returns
   *             true, the data is sent to that connection ID
   */
  @Override
  public void broadcastWhere(T data, Predicate<String> test) {
    // Send the data if the connection ID matches
    if (test.test(connection.getId())) {
      try {
        connection.send(data);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Closes the client's socket, disconnecting from the server
   *
   * @throws IOException if there was a networking error
   */
  public void close() throws IOException {
    socket.close();
  }
}
