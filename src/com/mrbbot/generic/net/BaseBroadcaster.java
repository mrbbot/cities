package com.mrbbot.generic.net;

abstract class BaseBroadcaster<T> implements Broadcaster<T> {
  @Override
  public final void broadcastExcluding(T data, String id) {
    broadcastWhere(data, (testId) -> !testId.equals(id));
  }

  @Override
  public final void broadcastTo(T data, String id) {
    broadcastWhere(data, (testId) -> testId.equals(id));
  }
}
