package com.mrbbot.generic.net;

public interface Handler<T> {
  void accept(Connection<T> connection, T data);
}
