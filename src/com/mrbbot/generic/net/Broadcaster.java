package com.mrbbot.generic.net;

import java.util.function.Predicate;

/**
 * Interface describing an object that can send packets to another place.
 *
 * @param <T> type of data to be exchanged over the network
 */
public interface Broadcaster<T> {
  /**
   * Broadcasts data to all connections
   *
   * @param data data to be sent
   */
  void broadcast(T data);

  /**
   * Broadcasts data to connections that return true from the predicate
   *
   * @param data data to be sent
   * @param test function to test each connection ID against, if it returns
   *             true, the data is sent to that connection ID
   */
  void broadcastWhere(T data, Predicate<String> test);

  /**
   * Broadcasts data to all but the specified ID
   *
   * @param data data to be sent
   * @param id   connection ID to exclude from sending
   */
  void broadcastExcluding(T data, String id);

  /**
   * Broadcasts data to only the specified ID
   *
   * @param data data to be sent
   * @param id   connection ID to send to
   */
  void broadcastTo(T data, String id);
}
