package com.mrbbot.generic.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.function.Predicate;

/**
 * Class representing a connection between a client and a server or vice-versa
 *
 * @param <T> type of data to be exchanged over the network
 */
public class Connection<T> implements Runnable, Broadcaster<T> {
  /**
   * Stream for sending data to the other side
   */
  private ObjectOutputStream outputStream;
  /**
   * Stream for receiving data from the other side
   */
  private ObjectInputStream inputStream;
  /**
   * Function to be called when an ID is received from the other side
   */
  private final IdHandler<T> idHandler;
  /**
   * Function to be called when data is received from the other side.
   */
  private final Handler<T> inputHandler;
  /**
   * Whether the connection is open and data is being sent.
   */
  private boolean open;
  /**
   * ID of this connection. Used for broadcast targeting.
   */
  private String id;
  /**
   * Thread that checks for data being sent and interprets it.
   */
  private Thread thread;
  /**
   * Broadcaster that actually handles sending data (the client or server)
   */
  private Broadcaster<T> broadcaster;

  /**
   * Constructor for creating a new connection
   *
   * @param socket       TCP socket for sending/receiving data to/from
   * @param idHandler    function to be called when the connection gets an ID
   * @param inputHandler function to be called when generic data is received
   * @param broadcaster  broadcaster for sending data to other connections
   * @throws IOException if there was an error creating in/output streams
   */
  Connection(
    Socket socket,
    IdHandler<T> idHandler,
    Handler<T> inputHandler,
    Broadcaster<T> broadcaster
  ) throws IOException {
    // Create streams for the socket
    outputStream = new ObjectOutputStream(socket.getOutputStream());
    inputStream = new ObjectInputStream(socket.getInputStream());

    // Store handlers
    this.idHandler = idHandler;
    this.inputHandler = inputHandler;
    this.broadcaster = broadcaster;

    open = true;

    // Create a new thread that constantly attempts to read data
    thread = new Thread(this);
    thread.setName("Connection");
    thread.start();
  }

  /**
   * Sends the specified data to the receiving end of this connection
   *
   * @param object data to be sent
   * @throws IOException if the data cannot be sent
   */
  public void send(Object object) throws IOException {
    // Log the send request
    System.out.println(String.format(
      "[%s] %s -> %s",
      new Date().toString(),
      object.getClass().getSimpleName(),
      id
    ));
    // Send the data and flush the stream so that it's sent immediately
    outputStream.writeObject(object);
    outputStream.flush();
  }

  /**
   * Runner for the input checking thread.
   */
  @Override
  public void run() {
    // Keep checking until the socket is closed
    while (open) {
      try {
        // Read the data, this will block the thread until data is received
        Object object = inputStream.readObject();
        // Log the incoming data
        System.out.println(String.format(
          "[%s] %s <- %s",
          new Date().toString(),
          object.getClass().getSimpleName(),
          id
        ));
        // If an ID hasn't been set yet, assume this is an ID
        if (id == null) {
          idHandler.accept(this, (String) object);
        } else {
          // Otherwise, this is generic data, so handle it accordingly
          //noinspection unchecked
          inputHandler.accept(this, (T) object);
        }
      } catch (IOException e) {
        // If there was an error close the connection
        System.out.println(String.format(
          "[%s] Connection with \"%s\" closed: %s",
          new Date().toString(),
          id,
          e.getMessage()
        ));
        // Send null to the handler to signal the connection closing
        inputHandler.accept(this, null);
        open = false;
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets this connection's ID or null if the ID hasn't been set yet
   *
   * @return this connection's ID
   */
  public String getId() {
    return id;
  }

  /**
   * Sets this connection's ID
   *
   * @param id new ID for this connection
   */
  void setId(String id) {
    this.id = id;
    thread.setName("Connection:" + id);
  }

  /**
   * Broadcasts data to all connections
   *
   * @param data data to be sent
   */
  @Override
  public void broadcast(T data) {
    broadcaster.broadcast(data);
  }

  /**
   * Broadcasts data to connections that return true from the predicate
   *
   * @param data data to be sent
   * @param test function to test each connection ID against, if it returns
   *             true, the data is sent to that connection ID
   */
  @Override
  public void broadcastWhere(T data, Predicate<String> test) {
    broadcaster.broadcastWhere(data, test);
  }

  /**
   * Broadcasts data to all but this connection
   *
   * @param data data to be sent
   */
  public void broadcastExcluding(T data) {
    broadcaster.broadcastExcluding(data, id);
  }

  /**
   * Broadcasts data to all but the specified ID
   *
   * @param data data to be sent
   * @param id   connection ID to exclude from sending
   */
  @Override
  public void broadcastExcluding(T data, String id) {
    broadcaster.broadcastExcluding(data, id);
  }

  /**
   * Broadcasts data to only this connection
   *
   * @param data data to be sent
   */
  public void broadcastTo(T data) {
    broadcaster.broadcastTo(data, id);
  }

  /**
   * Broadcasts data to only the specified ID
   *
   * @param data data to be sent
   * @param id   connection ID to send to
   */
  @Override
  public void broadcastTo(T data, String id) {
    broadcaster.broadcastTo(data, id);
  }
}
