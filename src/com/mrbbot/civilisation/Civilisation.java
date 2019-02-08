package com.mrbbot.civilisation;

import com.mrbbot.civilisation.logic.map.Map;
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
    //width = 1000;
    //height = 600;

    ScreenConnect screenConnect = new ScreenConnect(this);
    primaryStage.setScene(screenConnect.makeScene(primaryStage, width, height));

    primaryStage.setTitle("Civilisation");
    primaryStage.setResizable(false);
    primaryStage.setFullScreen(true);
    primaryStage.setOnCloseRequest((event) -> {
      try {
        if(CLIENT != null) CLIENT.close();
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

      if (data instanceof PacketMap) {
        Platform.runLater(() -> {
          screenGame = new ScreenGame(((PacketMap) data).map, id);
          primaryStage.setScene(screenGame.makeScene(primaryStage, width, height));

          // Add starting units
          Map map = screenGame.renderGame.root.data;
          int numPlayers = map.players.size();
          int gridWidth = map.hexagonGrid.getWidth();
          int gridHeight = map.hexagonGrid.getHeight();
          int x = gridWidth / 2;
          int y = gridHeight / 2;
          switch (numPlayers) {
            case 1:
              x = 1;
              y = 1;
              break;
            case 2:
              x = gridWidth - 3;
              y = gridHeight - 3;
              break;
            case 3:
              x = gridWidth - 3;
              y = 1;
              break;
            case 4:
              x = 1;
              y = gridHeight - 3;
              break;
          }

          PacketUnitCreate settlerCreate = new PacketUnitCreate(id, x, y, UnitType.SETTLER);
          PacketUnitCreate warriorCreate = new PacketUnitCreate(id, x + 1, y, UnitType.WARRIOR);
          screenGame.renderGame.handlePacket(settlerCreate);
          screenGame.renderGame.handlePacket(warriorCreate);
          CLIENT.broadcast(settlerCreate);
          CLIENT.broadcast(warriorCreate);
        });
      } else if(data instanceof PacketChat) {
        screenGame.handlePacketChat((PacketChat) data);
      } else {
        Platform.runLater(() -> screenGame.renderGame.handlePacket(data));
      }
    }));
    CLIENT.broadcast(new PacketInit());
  }

  public static void main(String[] args) {
    launch(args);
  }
}

