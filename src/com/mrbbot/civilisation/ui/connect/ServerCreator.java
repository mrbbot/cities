package com.mrbbot.civilisation.ui.connect;

import com.mrbbot.civilisation.logic.map.MapSize;

import java.io.IOException;

/**
 * Function called when the user requests that a server be started
 */
public interface ServerCreator {
  /**
   * Create server callback
   *
   * @param gameFilePath file path of the game save file (may or may not exist)
   * @param gameName     name of the game (if this is null, we're loading an
   *                     existing game from a file)
   * @param mapSize      desired map size of the new game (ignored if loading
   *                     from a file)
   * @param port         port number to run the server on
   * @throws IOException if the server cannot be created (e.g. port already
   *                     bound)
   */
  void createServer(
    String gameFilePath,
    String gameName,
    MapSize mapSize,
    int port
  ) throws IOException;
}
