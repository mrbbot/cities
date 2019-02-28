package com.mrbbot.civilisation.ui.connect;

import java.io.IOException;

/**
 * Function called when the user requests a connection be made to the server
 */
public interface ClientCreator {
  /**
   * Create client callback
   *
   * @param host server host IP/URL
   * @param port server port number
   * @param id   desired id of the player
   * @throws IOException if a connection cannot be established
   */
  void createClient(String host, int port, String id) throws IOException;
}
