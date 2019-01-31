package com.mrbbot.generic.net;

interface IdHandler<T> {
  void accept(Connection<T> connection, String data);
}
