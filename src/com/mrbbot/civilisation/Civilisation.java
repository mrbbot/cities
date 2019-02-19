package com.mrbbot.civilisation;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.civilisation.ui.connect.ConnectionRequestHandler;
import com.mrbbot.civilisation.ui.connect.ScreenConnect;
import com.mrbbot.civilisation.ui.game.ScreenGame;
import com.mrbbot.generic.net.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Civilisation
  extends Application implements ConnectionRequestHandler {

  public static Client<Packet> CLIENT;

  private Stage primaryStage;
  private int width, height;
  private ScreenGame screenGame;

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;

    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    width = (int) screenBounds.getWidth();
    height = (int) screenBounds.getHeight();
    width = 1000;
    height = 600;

    ScreenConnect screenConnect = new ScreenConnect(this);
    primaryStage.setScene(screenConnect.makeScene(primaryStage, width, height));

    primaryStage.setTitle("Civilisation");
    primaryStage.setResizable(false);
    //primaryStage.setFullScreen(true);
    primaryStage.setOnCloseRequest((event) -> {
      try {
        if (CLIENT != null) CLIENT.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    primaryStage.show();
  }

  @Override
  public void connect(String host, String id) throws IOException {
    CLIENT = new Client<>(host, 1234, id, ((connection, data) -> {
      System.out.println("Received \"" + data.getName() + "\" packet from \"" + connection.getId() + "\"...");

      Platform.runLater(() -> {
        if (data instanceof PacketGame) {
          Game game = new Game(((PacketGame) data).map);
          screenGame = new ScreenGame(game, id);
          primaryStage.setScene(screenGame.makeScene(primaryStage, width, height));
        } else if (data instanceof PacketChat) {
          screenGame.handlePacketChat((PacketChat) data);
        } else {
          if(data instanceof PacketReady) {
            screenGame.handlePacketReady((PacketReady) data);
          }
          screenGame.renderCivilisation.root.handlePacket(data);
        }
      });
    }));
    CLIENT.broadcast(new PacketInit());
  }

  public static void main(String[] args) {
    launch(args);
  }
}

