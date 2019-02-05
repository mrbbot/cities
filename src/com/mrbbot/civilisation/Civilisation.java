package com.mrbbot.civilisation;

import com.mrbbot.civilisation.net.packet.Packet;
import com.mrbbot.civilisation.net.packet.PacketInit;
import com.mrbbot.civilisation.net.packet.PacketMap;
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
    width = 1280;
    height = 720;

    ScreenConnect screenConnect = new ScreenConnect(this);
    primaryStage.setScene(screenConnect.makeScene(primaryStage, width, height));

/*
    StackPane pane = new StackPane();
    pane.setAlignment(Pos.TOP_LEFT);

    Map map = new Map();
    RenderMap renderMap = new RenderMap(map);
    RenderGame renderGame = new RenderGame(renderMap, width, height);

    UIGame ui = new UIGame(renderMap, width, height);
    ui.setPrefSize(width, height);

    pane.getChildren().addAll(renderGame.subScene, ui);
    Scene scene = new Scene(pane, width, height);
    renderGame.setScene(scene, e -> {
      if(e.getCode() == KeyCode.F11) {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
      }
    });*/

    primaryStage.setTitle("Civilisation");
    primaryStage.setResizable(false);
    //primaryStage.setScene(scene);
    //primaryStage.setFullScreen(true);
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
        });
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

