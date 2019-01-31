package com.mrbbot.civilisation.ui.connect;

import java.io.IOException;

public interface ConnectionRequestHandler {
  void connect(String host, String id) throws IOException;
}
