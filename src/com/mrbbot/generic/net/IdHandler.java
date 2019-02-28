package com.mrbbot.generic.net;

/**
 * Function to be called when a connection receives an ID
 *
 * @param <T> type of data being exchanged over the network
 */
interface IdHandler<T> {
  /**
   * Function called when a connection receives an ID
   *
   * @param connection the connection this ID is for
   * @param data       the request ID for this connection
   */
  void accept(Connection<T> connection, String data);
}
