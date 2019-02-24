package com.mrbbot.civilisation.ui.connect;

import com.mrbbot.civilisation.logic.map.MapSize;

import java.io.IOException;

public interface ServerCreator {
  void createServer(String gameFilePath, String gameName, MapSize mapSize, int port) throws IOException;
}
