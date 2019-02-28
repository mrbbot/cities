package com.mrbbot.civilisation;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.MapSize;
import com.mrbbot.civilisation.net.CivilisationServer;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.civilisation.ui.connect.ClientCreator;
import com.mrbbot.civilisation.ui.connect.ScreenConnect;
import com.mrbbot.civilisation.ui.connect.ServerCreator;
import com.mrbbot.civilisation.ui.game.ScreenGame;
import com.mrbbot.generic.net.Client;
import com.mrbbot.generic.net.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Civilisation extends Application implements ClientCreator, ServerCreator {
  public static Client<Packet> CLIENT;
  private static CivilisationServer SERVER;

  private Stage primaryStage;
  private int width, height;
  private ScreenGame screenGame;

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;

    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    width = (int) screenBounds.getWidth();
    height = (int) screenBounds.getHeight();
    width = 1000; //1000 //1600
    height = 600; //600 //900

    ScreenConnect screenConnect = new ScreenConnect(this, this);
    primaryStage.setScene(screenConnect.makeScene(primaryStage, width, height));

    primaryStage.setTitle("Civilisation");
    primaryStage.setResizable(false);
    //primaryStage.setFullScreen(true);
    primaryStage.setOnCloseRequest((event) -> {
      try {
        if (CLIENT != null) CLIENT.close();
      } catch (IOException ignored) { }
      try {
        if (SERVER != null) SERVER.close();
      } catch (IOException ignored) { }
      System.exit(0);
    });
    primaryStage.show();
  }

  public void createClient(String host, int port, String id) throws IOException {
    CLIENT = new Client<>(host, port, id, ((connection, data) -> Platform.runLater(() -> {
      if (data instanceof PacketGame) {
        Game game = new Game(((PacketGame) data).map);
        screenGame = new ScreenGame(game, id);
        primaryStage.setScene(screenGame.makeScene(primaryStage, width, height));
      } else if (data instanceof PacketChat) {
        screenGame.handlePacketChat((PacketChat) data);
      } else {
        if (data instanceof PacketReady) {
          screenGame.handlePacketReady((PacketReady) data);
        }
        screenGame.renderCivilisation.root.handlePacket(data);
      }
    })));
    CLIENT.broadcast(new PacketInit());
  }

  public void createServer(String gameFilePath, String gameName, MapSize mapSize, int port) throws IOException {
    if (gameName == null) {
      SERVER = new CivilisationServer(gameFilePath, port);
    } else {
      SERVER = new CivilisationServer(gameFilePath, gameName, mapSize, port);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}

