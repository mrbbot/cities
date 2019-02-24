package com.mrbbot.civilisation.ui.connect;

import java.io.IOException;

public interface ClientCreator {
  void createClient(String host, int port, String id) throws IOException;
}
