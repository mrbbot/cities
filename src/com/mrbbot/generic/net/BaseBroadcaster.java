package com.mrbbot.generic.net;

/**
 * Base class for a broadcaster containing implementations of some of the
 * functions that are the same for every broadcaster.
 *
 * @param <T> type of data to be exchanged over the network
 */
abstract class BaseBroadcaster<T> implements Broadcaster<T> {
  /**
   * Broadcasts data to all but the specified ID
   * @param data data to be sent
   * @param id connection ID to exclude from sending
   */
  @Override
  public final void broadcastExcluding(T data, String id) {
    broadcastWhere(data, (testId) -> !testId.equals(id));
  }

  /**
   * Broadcasts data to only the specified ID
   * @param data data to be sent
   * @param id connection ID to send to
   */
  @Override
  public final void broadcastTo(T data, String id) {
    broadcastWhere(data, (testId) -> testId.equals(id));
  }
}
