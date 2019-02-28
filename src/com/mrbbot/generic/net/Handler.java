package com.mrbbot.generic.net;

/**
 * Function to be called when incoming data is received.
 *
 * @param <T> type of data being exchanged over the network
 */
public interface Handler<T> {
  /**
   * Function called when incoming data is received
   *
   * @param connection the connection this data comes from
   * @param data       the data itself, or null if the connection has closed
   */
  void accept(Connection<T> connection, T data);
}
