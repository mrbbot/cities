package com.mrbbot.civilisation;

import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.render.RenderGame;
import com.mrbbot.civilisation.render.map.RenderMap;
import com.mrbbot.generic.render.RenderRoot;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Civilisation
  extends Application {
  private final static int WIDTH = 1280;
  private final static int HEIGHT = 720;

  @Override
  public void start(Stage primaryStage) {
    BorderPane borderPane = new BorderPane();

    Map map = new Map();
    RenderMap renderMap = new RenderMap(map);
    RenderGame renderGame = new RenderGame(renderMap, WIDTH, HEIGHT);

    borderPane.setCenter(renderGame.subScene);
    Scene scene = new Scene(borderPane, WIDTH, HEIGHT);
    renderGame.setScene(scene);
    primaryStage.setTitle("Civilisation");
    primaryStage.setResizable(false);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

}

