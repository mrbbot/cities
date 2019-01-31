package com.mrbbot.generic.net;

import java.util.function.Predicate;

public interface Broadcaster<T> {
  void broadcast(T data);
  void broadcastWhere(T data, Predicate<String> test);
  void broadcastExcluding(T data, String id);
  void broadcastTo(T data, String id);
}
